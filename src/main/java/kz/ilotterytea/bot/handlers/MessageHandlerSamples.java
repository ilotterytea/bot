package kz.ilotterytea.bot.handlers;

import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.SharedConstants;

import java.nio.charset.StandardCharsets;

/**
 * The samples for Twitch4j events
 * @author ilotterytea
 * @since 1.0
 */
public class MessageHandlerSamples {
    /**
     * Message handler sample for IRC message events.
     * @author ilotterytea
     * @since 1.0
     */
    public static void ircMessageEvent(IRCMessageEvent e) {
        if (!e.getMessage().isPresent()) return;

        String MSG = e.getMessage().get();
        final String PREFIX = Huinyabot.getProperties().getProperty("PREFIX", SharedConstants.DEFAULT_PREFIX);

        // Command processing:
        if (MSG.startsWith(PREFIX)) {
            MSG = MSG.substring(PREFIX.length());
            String cmdNameId = MSG.split(" ")[0];

            if (Huinyabot.getLoader().getCommands().containsKey(cmdNameId)) {
                String response = Huinyabot.getLoader().call(cmdNameId, e);

                if (response != null) {
                    Huinyabot.getClient().getChat().sendMessage(
                            e.getChannel().getName(),
                            // idk why just a string is not encoded utf-8
                            new String(response.getBytes(), StandardCharsets.UTF_8)
                    );
                }
            }
        }
    }
}
