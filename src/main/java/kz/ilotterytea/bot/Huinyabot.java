package kz.ilotterytea.bot;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import kz.ilotterytea.bot.api.commands.CommandLoader;
import kz.ilotterytea.bot.api.delay.DelayManager;
import kz.ilotterytea.bot.storage.PropLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

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
        OAuth2Credential credential = new OAuth2Credential("twitch", properties.getProperty("OAUTH2_TOKEN", ""));
        properties.setProperty("HB_BOT_USERNAME", (credential.getUserName() != null) ? credential.getUserName() : "");

        client = TwitchClientBuilder.builder()
                .withChatAccount(credential)
                .withEnableTMI(true)
                .withEnableChat(true)
                .build();

        client.getChat().connect();
        if (properties.getProperty("HB_BOT_USERNAME") != null) {
            client.getChat().joinChannel(properties.getProperty("HB_BOT_USERNAME"));
        }
        client.getChat().joinChannel("ilotterytea");

        client.getEventManager().onEvent(IRCMessageEvent.class, e -> {
            if (e.getMessage().isPresent() && e.getMessage().get().equals("test")) {
                client.getChat().sendMessage(e.getChannel().getName(), "test successfully completed");
            }
        });
    }

    @Override
    public void dispose() {
        client.close();
    }
}
