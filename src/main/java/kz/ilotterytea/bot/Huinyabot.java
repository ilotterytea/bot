package kz.ilotterytea.bot;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import com.github.twitch4j.helix.domain.User;
import kz.ilotterytea.bot.api.commands.CommandLoader;
import kz.ilotterytea.bot.api.delay.DelayManager;
import kz.ilotterytea.bot.handlers.MessageHandlerSamples;
import kz.ilotterytea.bot.storage.PropLoader;
import kz.ilotterytea.bot.utils.StorageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

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

    private final Logger LOGGER = LoggerFactory.getLogger(Huinyabot.class);

    public TwitchClient getClient() { return client; }
    public Properties getProperties() { return properties; }
    public CommandLoader getLoader() { return loader; }
    public DelayManager getDelayer() { return delayer; }

    private static Huinyabot instance;
    public static Huinyabot getInstance() { return instance; }
    public Huinyabot() { instance = this; }

    @Override
    public void init() {
        StorageUtils.checkIntegrity();

        properties = new PropLoader(SharedConstants.PROPERTIES_PATH);
        loader = new CommandLoader();
        delayer = new DelayManager();

        // - - -  T W I T C H  C L I E N T  - - - :
        OAuth2Credential credential = new OAuth2Credential("twitch", properties.getProperty("OAUTH2_TOKEN"));

        client = TwitchClientBuilder.builder()
                .withChatAccount(credential)
                .withEnableTMI(true)
                .withEnableChat(true)
                .withClientId(properties.getProperty("CLIENT_ID"))
                .withEnableHelix(true)
                .build();

        client.getChat().connect();
        if (credential.getUserName() != null) {
            client.getChat().joinChannel(credential.getUserName());
        }
        if (client.getHelix() != null && properties.getProperty("ACCESS_TOKEN") != null && properties.getProperty("INITIAL_CHANNEL_IDS") != null) {
            List<User> userList = client.getHelix().getUsers(
                    properties.getProperty("ACCESS_TOKEN"),
                    Arrays.stream(properties.getProperty("INITIAL_CHANNEL_IDS").split(",")).collect(Collectors.toList()),
                    null
            ).execute().getUsers();

            for (User u : userList) {
                client.getChat().joinChannel(u.getLogin());
            }
        }

        client.getEventManager().onEvent(IRCMessageEvent.class, MessageHandlerSamples::ircMessageEvent);
    }

    @Override
    public void dispose() {
        client.close();
    }
}
