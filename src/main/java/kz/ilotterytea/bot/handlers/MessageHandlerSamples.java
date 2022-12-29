package kz.ilotterytea.bot.handlers;

import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.SharedConstants;

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

        final String MSG = e.getMessage().get();
        final String PREFIX = Huinyabot.getProperties().getProperty("PREFIX", SharedConstants.DEFAULT_PREFIX);

        // Command processing:
        if (MSG.startsWith(PREFIX)) {
            String cmdNameId = MSG.substring(PREFIX.length(), MSG.split(" ")[0].length());

            if (Huinyabot.getLoader().getCommands().containsKey(cmdNameId)) {
                String response = Huinyabot.getLoader().call(cmdNameId, e);

                if (response != null) {
                    Huinyabot.getClient().getChat().sendMessage(e.getChannel().getName(), response);
                }
            }
        }
    }
}
