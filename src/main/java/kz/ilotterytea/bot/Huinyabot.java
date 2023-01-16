package kz.ilotterytea.bot;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.DeleteMessageEvent;
import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import com.github.twitch4j.chat.events.channel.UserBanEvent;
import com.github.twitch4j.events.ChannelChangeGameEvent;
import com.github.twitch4j.events.ChannelChangeTitleEvent;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.events.ChannelGoOfflineEvent;
import com.github.twitch4j.helix.domain.User;
import com.google.gson.Gson;
import kz.ilotterytea.bot.api.commands.CommandLoader;
import kz.ilotterytea.bot.api.delay.DelayManager;
import kz.ilotterytea.bot.fun.markov.MarkovChainHandler;
import kz.ilotterytea.bot.handlers.MessageHandlerSamples;
import kz.ilotterytea.bot.i18n.I18N;
import kz.ilotterytea.bot.models.TargetModel;
import kz.ilotterytea.bot.models.emotes.Emote;
import kz.ilotterytea.bot.models.emotes.Provider;
import kz.ilotterytea.bot.storage.PropLoader;
import kz.ilotterytea.bot.storage.json.TargetController;
import kz.ilotterytea.bot.storage.json.UserController;
import kz.ilotterytea.bot.thirdpartythings.seventv.v1.SevenTVEmoteLoader;
import kz.ilotterytea.bot.thirdpartythings.seventv.v1.SevenTVWebsocketClient;
import kz.ilotterytea.bot.thirdpartythings.seventv.v1.models.EmoteAPIData;
import kz.ilotterytea.bot.thirdpartythings.seventv.v1.models.Message;
import kz.ilotterytea.bot.utils.StorageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.net.URISyntaxException;

/**
 * Bot.
 * @author ilotterytea
 * @since 1.0
 */
public class Huinyabot extends Bot {
    private Properties properties;
    private TwitchClient client;
    private CommandLoader loader;
    private DelayManager delayer;
    private TargetController targets;
    private UserController users;
    private SevenTVWebsocketClient sevenTV;
    private Map<String, String> targetLinks;
    private MarkovChainHandler markov;
    private OAuth2Credential credential;
    private I18N i18N;

    private final Logger LOGGER = LoggerFactory.getLogger(Huinyabot.class);

    public TwitchClient getClient() { return client; }
    public Properties getProperties() { return properties; }
    public CommandLoader getLoader() { return loader; }
    public DelayManager getDelayer() { return delayer; }
    public TargetController getTargetCtrl() { return targets; }
    public UserController getUserCtrl() { return users; }
    public SevenTVWebsocketClient getSevenTVWSClient() { return sevenTV; }
    public Map<String, String> getTargetLinks() { return targetLinks; }
    public MarkovChainHandler getMarkov() { return markov; }
    public OAuth2Credential getCredential() { return credential; }
    public I18N getLocale() { return i18N; }

    private static Huinyabot instance;
    public static Huinyabot getInstance() { return instance; }
    public Huinyabot() { instance = this; }

    @Override
    public void init() {
        StorageUtils.checkIntegrity();
        targets = new TargetController(SharedConstants.TARGETS_DIR);
        users = new UserController(SharedConstants.USERS_DIR);

        properties = new PropLoader(SharedConstants.PROPERTIES_PATH);
        loader = new CommandLoader();
        delayer = new DelayManager();
        targetLinks = new HashMap<>();

        i18N = new I18N(new StorageUtils().getFileFromResource("i18n"));

        markov = new MarkovChainHandler(SharedConstants.CHAINS_FILE);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                targets.save();
                users.save();
                markov.save();
            }
        }, 300000, 300000);

        try {
            sevenTV = new SevenTVWebsocketClient();
            sevenTV.connectBlocking();
        } catch (URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        ArrayList<EmoteAPIData> globalEmotes = new SevenTVEmoteLoader().getGlobalEmotes();

        // - - -  T W I T C H  C L I E N T  - - - :
        credential = new OAuth2Credential("twitch", properties.getProperty("OAUTH2_TOKEN"));

        client = TwitchClientBuilder.builder()
                .withChatAccount(credential)
                .withDefaultAuthToken(credential)
                .withEnableTMI(true)
                .withEnableChat(true)
                .withClientId(properties.getProperty("CLIENT_ID"))
                .withEnableHelix(true)
                .build();

        client.getChat().connect();
        if (credential.getUserName() != null && credential.getUserId() != null) {
            client.getChat().joinChannel(credential.getUserName());
            targetLinks.put(credential.getUserName(), credential.getUserId());

            if (!Huinyabot.getInstance().targets.getAll().containsKey(credential.getUserId())) {
                Huinyabot.getInstance().targets.set(credential.getUserId(), Huinyabot.getInstance().targets.getOrDefault(credential.getUserId()));
            }

            ArrayList<EmoteAPIData> channelEmotes = new SevenTVEmoteLoader().getChannelEmotes(credential.getUserName());

            if (channelEmotes != null) {
                if (!Huinyabot.getInstance().getTargetCtrl().get(credential.getUserId()).getEmotes().containsKey(Provider.SEVENTV)) {
                    Huinyabot.getInstance().getTargetCtrl().get(credential.getUserId()).getEmotes().put(Provider.SEVENTV, new HashMap<>());
                }

                for (EmoteAPIData emote : channelEmotes) {
                    if (!Huinyabot.getInstance().getTargetCtrl().get(credential.getUserId()).getEmotes().get(Provider.SEVENTV).containsKey(emote.getId())) {
                        Huinyabot.getInstance().getTargetCtrl().get(credential.getUserId()).getEmotes().get(Provider.SEVENTV).put(
                                emote.getId(),
                                new Emote(
                                        emote.getId(),
                                        Provider.SEVENTV,
                                        emote.getName(),
                                        0,
                                        false,
                                        false
                                )
                        );
                    }
                }

                Huinyabot.getInstance().getSevenTVWSClient().send(
                        new Gson().toJson(new Message("join", credential.getUserName()))
                );
            }

            if (globalEmotes != null) {
                if (!Huinyabot.getInstance().getTargetCtrl().get(credential.getUserId()).getEmotes().containsKey(Provider.SEVENTV)) {
                    Huinyabot.getInstance().getTargetCtrl().get(credential.getUserId()).getEmotes().put(Provider.SEVENTV, new HashMap<>());
                }

                for (EmoteAPIData emote : globalEmotes) {
                    if (!Huinyabot.getInstance().getTargetCtrl().get(credential.getUserId()).getEmotes().get(Provider.SEVENTV).containsKey(emote.getId())) {
                        Huinyabot.getInstance().getTargetCtrl().get(credential.getUserId()).getEmotes().get(Provider.SEVENTV).put(
                                emote.getId(),
                                new Emote(
                                        emote.getId(),
                                        Provider.SEVENTV,
                                        emote.getName(),
                                        0,
                                        true,
                                        false
                                )
                        );
                    }
                }
            }
        }

        List<User> userList = new ArrayList<>();
        if (client.getHelix() != null && properties.getProperty("ACCESS_TOKEN") != null && targets.getAll().keySet().size() > 0) {
            userList = client.getHelix().getUsers(
                    properties.getProperty("ACCESS_TOKEN"),
                    new ArrayList<>(targets.getAll().keySet()),
                    null
            ).execute().getUsers();

            for (User u : userList) {
                if (!client.getChat().isChannelJoined(u.getLogin())) {
                    client.getChat().joinChannel(u.getLogin());
                }

                targetLinks.put(u.getLogin(), u.getId());
            }
        }

        for (User user : userList) {
            sevenTV.send(new Gson().toJson(new Message("join", user.getLogin())));

            if (!targets.get(user.getId()).getEmotes().containsKey(Provider.SEVENTV)) {
                targets.get(user.getId()).getEmotes().put(Provider.SEVENTV, new HashMap<>());
            }

            // Update the channel emotes:
            ArrayList<EmoteAPIData> channelEmotes = new SevenTVEmoteLoader().getChannelEmotes(user.getLogin());
            if (channelEmotes != null) {
                for (EmoteAPIData emote : channelEmotes) {
                    if (!targets.get(user.getId()).getEmotes().get(Provider.SEVENTV).containsKey(emote.getId())) {
                        targets.get(user.getId()).getEmotes().get(Provider.SEVENTV).put(
                                emote.getId(),
                                new Emote(
                                        emote.getId(),
                                        Provider.SEVENTV,
                                        emote.getName(),
                                        0,
                                        false,
                                        false
                                )
                        );
                    }
                }
            }
        }

        if (globalEmotes != null) {
            for (EmoteAPIData emote : globalEmotes) {
                for (TargetModel target : targets.getAll().values()) {
                    if (!targets.get(target.getAliasId()).getEmotes().containsKey(Provider.SEVENTV)) {
                        targets.get(target.getAliasId()).getEmotes().put(Provider.SEVENTV, new HashMap<>());
                    }

                    if (!targets.get(target.getAliasId()).getEmotes().get(Provider.SEVENTV).containsKey(emote.getId())) {
                        targets.get(target.getAliasId()).getEmotes().get(Provider.SEVENTV).put(
                                emote.getId(),
                                new Emote(
                                        emote.getId(),
                                        Provider.SEVENTV,
                                        emote.getName(),
                                        0,
                                        true,
                                        false
                                )
                        );
                    }
                }
            }
        }

        ArrayList<String> listeningNow = new ArrayList<>();

        for (TargetModel target : targets.getAll().values()) {
            if (target.getListeners().keySet().size() != 0) {
                List<User> users = client.getHelix().getUsers(
                        properties.getProperty("ACCESS_TOKEN", null),
                        new ArrayList<>(target.getListeners().keySet()),
                        null
                ).execute().getUsers();

                for (User user : users) {
                    if (!listeningNow.contains(user.getLogin())) {
                        client.getClientHelper().enableStreamEventListener(user.getId(), user.getLogin());
                        listeningNow.add(user.getLogin());
                        LOGGER.debug("Listening for stream events for user " + user.getLogin());
                    }
                }
            }
        }

        client.getEventManager().onEvent(IRCMessageEvent.class, MessageHandlerSamples::ircMessageEvent);
        client.getEventManager().onEvent(DeleteMessageEvent.class, MessageHandlerSamples::deleteMessageEvent);
        client.getEventManager().onEvent(UserBanEvent.class, MessageHandlerSamples::userBanEvent);

        client.getEventManager().onEvent(ChannelGoLiveEvent.class, MessageHandlerSamples::goLiveEvent);
        client.getEventManager().onEvent(ChannelGoOfflineEvent.class, MessageHandlerSamples::goOfflineEvent);
        client.getEventManager().onEvent(ChannelChangeGameEvent.class, MessageHandlerSamples::changeGameEvent);
        client.getEventManager().onEvent(ChannelChangeTitleEvent.class, MessageHandlerSamples::changeTitleEvent);
    }

    @Override
    public void dispose() {
        client.close();
        sevenTV.close();
        targets.save();
        users.save();
        markov.save();
    }
}
