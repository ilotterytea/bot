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
    private static Properties properties;
    private static TwitchClient client;
    private static CommandLoader loader;
    private static DelayManager delayer;

    private final Logger LOGGER = LoggerFactory.getLogger(Huinyabot.class);

    public static TwitchClient getClient() { return client; }
    public static Properties getProperties() { return properties; }
    public static CommandLoader getLoader() { return loader; }
    public static DelayManager getDelayer() { return delayer; }

    @Override
    public void init() {
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
