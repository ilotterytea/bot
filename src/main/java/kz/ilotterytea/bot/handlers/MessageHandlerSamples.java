package kz.ilotterytea.bot.handlers;

import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.SharedConstants;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.models.ArgumentsModel;
import kz.ilotterytea.bot.models.MessageModel;

/**
 * The samples for Twitch4j events
 * @author ilotterytea
 * @since 1.0
 */
public class MessageHandlerSamples {
    private static final Huinyabot bot = Huinyabot.getInstance();

    /**
     * Message handler sample for IRC message events.
     * @author ilotterytea
     * @since 1.0
     */
    public static void ircMessageEvent(IRCMessageEvent e) {
        if (!e.getMessage().isPresent()) return;

        String MSG = e.getMessage().get();
        final String PREFIX = bot.getProperties().getProperty("PREFIX", SharedConstants.DEFAULT_PREFIX);
        final ArgumentsModel args = new ArgumentsModel(
                bot.getUserCtrl().getOrDefault(e.getUserId()),
                MessageModel.create(e.getMessage().get(), PREFIX),
                e
        );

        // Command processing:
        if (MSG.startsWith(PREFIX)) {
            MSG = MSG.substring(PREFIX.length());
            String cmdNameId = MSG.split(" ")[0];

            if (bot.getLoader().getCommands().containsKey(cmdNameId)) {
                String response = bot.getLoader().call(cmdNameId, args);

                if (response != null) {
                    bot.getClient().getChat().sendMessage(
                            e.getChannel().getName(),
                            response,
                            null,
                            (e.getMessageId().isEmpty()) ? null : e.getMessageId().get()
                    );
                }
            } else {
                for (Command cmd : bot.getLoader().getCommands().values()) {
                    for (String alias : cmd.getAliases()) {
                        if (alias.equals(cmdNameId)) {
                            String response = bot.getLoader().call(cmd.getNameId(), args);

                            if (response != null) {
                                bot.getClient().getChat().sendMessage(
                                        e.getChannel().getName(),
                                        response,
                                        null,
                                        (e.getMessageId().isEmpty()) ? null : e.getMessageId().get()
                                );
                            }
                        }
                    }
                }
            }
        }
    }
}
