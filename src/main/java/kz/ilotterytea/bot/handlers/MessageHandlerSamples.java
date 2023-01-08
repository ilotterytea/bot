package kz.ilotterytea.bot.handlers;

import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.SharedConstants;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.api.permissions.Permissions;
import kz.ilotterytea.bot.models.ArgumentsModel;
import kz.ilotterytea.bot.models.CustomCommand;
import kz.ilotterytea.bot.models.MessageModel;
import kz.ilotterytea.bot.models.TargetModel;
import kz.ilotterytea.bot.models.emotes.Emote;
import kz.ilotterytea.bot.models.emotes.Provider;

import java.util.Objects;

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
        if (
                !e.getMessage().isPresent() ||
                bot.getUserCtrl().getOrDefault(e.getUserId()).isSuspended()
        ) {
            return;
        }

        TargetModel target = bot.getTargetCtrl().get(e.getChannel().getId());

        bot.getTargetCtrl().get(e.getChannel().getId()).getStats().setChatLines(
                target.getStats().getChatLines() + 1
        );

        if (target.getListeningMode()) {
            return;
        }

        String MSG = e.getMessage().get();
        final String PREFIX = bot.getProperties().getProperty("PREFIX", SharedConstants.DEFAULT_PREFIX);
        final ArgumentsModel args = new ArgumentsModel(
                bot.getUserCtrl().getOrDefault(e.getUserId()),
                Permissions.USER,
                MessageModel.create(e.getMessage().get(), PREFIX),
                e
        );

        // Set the user's current permissions:
        if (bot.getUserCtrl().getOrDefault(e.getUserId()).isSuperUser()) {
            args.setCurrentPermissions(Permissions.SUPAUSER);
        } else if (Objects.equals(e.getChannel().getId(), e.getUser().getId())) {
            args.setCurrentPermissions(Permissions.BROADCASTER);
        } else if (e.getBadges().containsKey("moderator")) {
            args.setCurrentPermissions(Permissions.MOD);
        } else if (e.getBadges().containsKey("vip")) {
            args.setCurrentPermissions(Permissions.VIP);
        }

        if (target != null) {
            // Emote counter update:
            if (target.getEmotes().containsKey(Provider.SEVENTV)) {
                for (String word : MSG.split(" ")) {
                    for (Emote em : target.getEmotes().get(Provider.SEVENTV).values()) {
                        if (Objects.equals(word, em.getName())) {
                            em.setCount(em.getCount() + 1);
                            break;
                        }
                    }
                }
            }
        }

        if (Objects.equals(MSG, "test")) {
            bot.getTargetCtrl().get(e.getChannel().getId()).getStats().setSuccessfulTests(
                    target.getStats().getSuccessfulTests() + 1
            );

            bot.getClient().getChat().sendMessage(
                    e.getChannel().getName(),
                    String.format(
                            "test %s successfully completed!",
                            bot.getTargetCtrl().get(e.getChannel().getId()).getStats().getSuccessfulTests()
                    )
            );
            return;
        }

        // Command processing:
        if (MSG.startsWith(PREFIX)) {
            final String MSG2 = MSG.substring(PREFIX.length());
            String cmdNameId = MSG2.split(" ")[0];

            if (bot.getLoader().getCommands().containsKey(cmdNameId)) {
                String response = bot.getLoader().call(cmdNameId, args);

                if (response != null) {
                    bot.getClient().getChat().sendMessage(
                            e.getChannel().getName(),
                            response,
                            null,
                            (!e.getMessageId().isPresent()) ? null : e.getMessageId().get()
                    );
                    bot.getTargetCtrl().get(e.getChannel().getId()).getStats().setExecutedCommandsCount(
                            target.getStats().getExecutedCommandsCount() + 1
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
                                        (!e.getMessageId().isPresent()) ? null : e.getMessageId().get()
                                );
                            }
                        }
                    }
                }
            }
        }

        // Custom command processing:
        if (bot.getTargetCtrl().get(e.getChannel().getId()).getCustomCommands().containsKey(MSG)) {
            CustomCommand cmd = bot.getTargetCtrl().get(e.getChannel().getId()).getCustomCommands().get(MSG);

            if (cmd.getValue()) {
                bot.getClient().getChat().sendMessage(
                        e.getChannel().getName(),
                        cmd.getResponse(),
                        null,
                        (!e.getMessageId().isPresent()) ? null : e.getMessageId().get()
                );
                bot.getTargetCtrl().get(e.getChannel().getId()).getStats().setExecutedCommandsCount(
                        target.getStats().getExecutedCommandsCount() + 1
                );
            }
        }
    }
}
