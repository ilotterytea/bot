package kz.ilotterytea.bot.handlers;

import com.github.twitch4j.chat.events.channel.DeleteMessageEvent;
import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import com.github.twitch4j.chat.events.channel.UserBanEvent;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.SharedConstants;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.api.permissions.Permissions;
import kz.ilotterytea.bot.fun.markov.ChatChain;
import kz.ilotterytea.bot.models.ArgumentsModel;
import kz.ilotterytea.bot.models.CustomCommand;
import kz.ilotterytea.bot.models.MessageModel;
import kz.ilotterytea.bot.models.TargetModel;
import kz.ilotterytea.bot.models.emotes.Emote;
import kz.ilotterytea.bot.models.emotes.Provider;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The samples for Twitch4j events
 * @author ilotterytea
 * @since 1.0
 */
public class MessageHandlerSamples {
    private static final Huinyabot bot = Huinyabot.getInstance();
    private static final Pattern markovUsernamePattern = Pattern.compile(
            "((@)?"+Huinyabot.getInstance().getCredential().getUserName().toLowerCase()+"(,)?)",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern markovMessagePattern = Pattern.compile(
            "^" + markovUsernamePattern.pattern() + ".*",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE
    );
    private static final Pattern markovURLPattern = Pattern.compile(
            "([A-Za-z]+:\\/\\/)?[A-Za-z0-9\\-_]+\\.[A-Za-z0-9\\-_:%&;\\?\\#\\/.=]+",
            Pattern.CASE_INSENSITIVE
    );

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

        // Emote counter update:
        if (target.getEmotes().containsKey(Provider.SEVENTV)) {
            for (String word : e.getMessage().get().split(" ")) {
                for (Emote em : target.getEmotes().get(Provider.SEVENTV).values()) {
                    if (Objects.equals(word, em.getName())) {
                        em.setCount(em.getCount() + 1);
                        break;
                    }
                }
            }
        }

        if (target.getListeningMode()) {
            bot.getMarkov().scanText(
                    e.getMessage().get(),
                    (e.getMessageId().isPresent()) ? e.getMessageId().get() : null,
                    e.getChannel().getId(),
                    e.getUser().getId()
            );
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
                        (!e.getMessageId().isPresent() || cmd.getFlag("no-mention")) ? null : e.getMessageId().get()
                );
                bot.getTargetCtrl().get(e.getChannel().getId()).getStats().setExecutedCommandsCount(
                        target.getStats().getExecutedCommandsCount() + 1
                );
            }
        }

        // Markov processing:
        if (markovMessagePattern.matcher(MSG).find()) {
            MSG = markovUsernamePattern.matcher(MSG).replaceAll("");
            MSG = markovURLPattern.matcher(MSG).replaceAll("");
            String generatedText = Huinyabot.getInstance().getMarkov().generateText(MSG);
            generatedText = markovUsernamePattern.matcher(generatedText).replaceAll("");
            generatedText = markovURLPattern.matcher(generatedText).replaceAll("");

            if (generatedText.length() > 500) {
                generatedText = generatedText.substring(0, 497) + "...";
            }

            bot.getClient().getChat().sendMessage(
                    e.getChannel().getName(),
                    generatedText,
                    null,
                    (!e.getMessageId().isPresent()) ? null : e.getMessageId().get()
            );
        } else {
            Huinyabot.getInstance().getMarkov().scanText(
                    MSG,
                    (e.getMessageId().isPresent()) ? e.getMessageId().get() : null,
                    e.getChannel().getId(),
                    e.getUser().getId()
            );
        }
    }

    /**
     * Message handler sample for delete message events.
     * @author ilotterytea
     * @since 1.2.2
     */
    public static void deleteMessageEvent(DeleteMessageEvent e) {
        List<ChatChain> chains = bot.getMarkov().getRecords()
                .stream()
                .filter(c -> Objects.equals(c.getToWordAuthor().getMsgId(), e.getMsgId()))
                .collect(Collectors.toList());

        chains.addAll(
                bot.getMarkov().getRecords()
                        .stream()
                        .filter(c -> Objects.equals(c.getFromWordAuthor().getMsgId(), e.getMsgId()))
                        .collect(Collectors.toList())
        );

        for (ChatChain chain : chains) {
            int i = bot.getMarkov().getRecords().indexOf(chain);

            if (i > -1) {
                bot.getMarkov().getRecords().remove(i);
            }
        }
    }

    /**
     * Message handler sample for user ban events.
     * @author ilotterytea
     * @since 1.2.2
     */
    public static void userBanEvent(UserBanEvent e) {
        List<ChatChain> chains = bot.getMarkov().getRecords()
                .stream()
                .filter(c -> Objects.equals(c.getToWordAuthor().getChannelId(), e.getChannel().getId()) && Objects.equals(c.getToWordAuthor().getUserId(), e.getUser().getId()))
                .collect(Collectors.toList());

        chains.addAll(
                bot.getMarkov().getRecords()
                        .stream()
                        .filter(c -> Objects.equals(c.getFromWordAuthor().getChannelId(), e.getChannel().getId()) && Objects.equals(c.getFromWordAuthor().getUserId(), e.getUser().getId()))
                        .collect(Collectors.toList())
        );

        for (ChatChain chain : chains) {
            int i = bot.getMarkov().getRecords().indexOf(chain);

            if (i > -1) {
                bot.getMarkov().getRecords().remove(i);
            }
        }
    }
}
