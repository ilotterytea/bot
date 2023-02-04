package kz.ilotterytea.bot.handlers;

import com.github.twitch4j.chat.events.channel.DeleteMessageEvent;
import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import com.github.twitch4j.chat.events.channel.UserBanEvent;
import com.github.twitch4j.events.ChannelChangeGameEvent;
import com.github.twitch4j.events.ChannelChangeTitleEvent;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.events.ChannelGoOfflineEvent;
import com.github.twitch4j.helix.domain.User;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.SharedConstants;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.api.permissions.Permissions;
import kz.ilotterytea.bot.fun.markov.ChatChain;
import kz.ilotterytea.bot.i18n.LineIds;
import kz.ilotterytea.bot.models.*;
import kz.ilotterytea.bot.models.emotes.Emote;
import kz.ilotterytea.bot.models.emotes.Provider;
import kz.ilotterytea.bot.models.notify.NotifyListener;
import kz.ilotterytea.bot.models.notify.NotifySubscriber;

import java.util.*;
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
                bot.getUserCtrl().getOrDefault(e.getUserId()).getFlags().contains("suspended")
        ) {
            return;
        }

        TargetModel target = bot.getTargetCtrl().get(e.getChannel().getId());
        UserModel user = bot.getUserCtrl().get(e.getUser().getId());

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

        if (target.getFlags().contains("listen-only")) {
            bot.getMarkov().scanText(
                    e.getMessage().get(),
                    (e.getMessageId().isPresent()) ? e.getMessageId().get() : null,
                    e.getChannel().getId(),
                    e.getUser().getId()
            );
            return;
        }

        String MSG = e.getMessage().get();
        final String PREFIX = (Huinyabot.getInstance().getTargetCtrl().get(e.getChannel().getId()).getPrefix() == null) ?
                bot.getProperties().getProperty("PREFIX", SharedConstants.DEFAULT_PREFIX) : Huinyabot.getInstance().getTargetCtrl().get(e.getChannel().getId()).getPrefix();
        final ArgumentsModel args = new ArgumentsModel(
                bot.getUserCtrl().getOrDefault(e.getUserId()),
                Permissions.USER,
                (Huinyabot.getInstance().getTargetCtrl().get(e.getChannel().getId()).getLanguage() == null) ?
                        bot.getProperties().getProperty("DEFAULT_LANGUAGE", SharedConstants.DEFAULT_LOCALE_ID) : Huinyabot.getInstance().getTargetCtrl().get(e.getChannel().getId()).getLanguage(),
                MessageModel.create(e.getMessage().get(), PREFIX),
                e
        );

        // Set the user's current permissions:
        if (user != null && user.getFlags().contains("superuser")) {
            args.setCurrentPermissions(Permissions.SUPAUSER);
        } else if (Objects.equals(e.getChannel().getId(), e.getUser().getId())) {
            args.setCurrentPermissions(Permissions.BROADCASTER);
        } else if (e.getBadges().containsKey("moderator")) {
            args.setCurrentPermissions(Permissions.MOD);
        } else if (e.getBadges().containsKey("vip")) {
            args.setCurrentPermissions(Permissions.VIP);
        }

        if (user != null && user.getLanguage() != null) {
            args.setLanguage(user.getLanguage());
        } else if (target.getLanguage() != null){
            args.setLanguage(target.getLanguage());
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

    public static void goLiveEvent(ChannelGoLiveEvent e) {
        Map<String, String> revLinks = Huinyabot.getInstance().getTargetLinks()
                .entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getValue,
                                Map.Entry::getKey,
                                (l, r) -> l,
                                LinkedHashMap::new
                        )
                );


        for (TargetModel target : Huinyabot.getInstance().getTargetCtrl().getAll().values()
                .stream()
                .filter(t->t.getListeners().containsKey(e.getChannel().getId()) && t.getListeners().get(e.getChannel().getId()).getEvents().contains("live"))
                .collect(Collectors.toList())
        ) {
            NotifyListener listener = target.getListeners().get(e.getStream().getUserId());

            ArrayList<String> msgs = new ArrayList<>();
            int index = 1;
            msgs.add(listener.getMessages().getOrDefault(
                    "live",
                    bot.getLocale().literalText(
                            target.getLanguage(),
                            LineIds.WENT_LIVE_NOTIFICATION
                    )
            ).replaceAll(
                    "%\\{name}",
                    e.getChannel().getName()
            ).replaceAll(
                    "%\\{title}",
                    (Objects.equals(e.getStream().getTitle(), "")) ? "N/A" : e.getStream().getTitle()
            ).replaceAll(
                    "%\\{game}",
                    (Objects.equals(e.getStream().getGameName(), "")) ? "N/A" : e.getStream().getGameName()
            ));
            msgs.add("");

            if (listener.getFlags().containsKey("live") && listener.getFlags().get("live").contains("massping")) {
                List<String> chatters = bot.getClient().getMessagingInterface().getChatters(
                        revLinks.get(target.getAliasId())
                ).execute().getAllViewers();

                for (String chatter : chatters) {
                    StringBuilder sb = new StringBuilder();

                    if (
                            (
                                    listener.getIcons().getOrDefault("live", "") +
                                            " " +
                                            msgs.get(index) +
                                            "@" +
                                            chatter +
                                            " " +
                                            listener.getIcons().getOrDefault("live", "")
                            ).length() < 500
                    ) {
                        sb
                                .append(msgs.get(index))
                                .append("@")
                                .append(chatter)
                                .append(" ");

                        msgs.remove(index);
                        msgs.add(index, sb.toString());
                    } else {
                        msgs.add("");
                        index++;
                    }
                }
            } else {
                List<User> subUsers = bot.getClient().getHelix().getUsers(
                        bot.getProperties().getProperty("ACCESS_TOKEN", null),
                        listener.getSubscribers().stream().map(NotifySubscriber::getAliasId).collect(Collectors.toList()),
                        null
                ).execute().getUsers();

                for (NotifySubscriber subscriber : listener.getSubscribers()
                        .stream().filter(sub->sub.getSubscribedEvents().contains("live")).collect(Collectors.toList())) {
                    User user = subUsers
                            .stream()
                            .filter(sub->Objects.equals(sub.getId(), subscriber.getAliasId()))
                            .findFirst().orElse(null);

                    if (user != null) {
                        StringBuilder sb = new StringBuilder();

                        if (
                                (
                                        listener.getIcons().getOrDefault("live", "") +
                                                " " +
                                                msgs.get(index) +
                                                "@" +
                                                user.getLogin() +
                                                " " +
                                                listener.getIcons().getOrDefault("live", "")
                                ).length() < 500
                        ) {
                            sb
                                    .append(msgs.get(index))
                                    .append("@")
                                    .append(user.getLogin())
                                    .append(" ");

                            msgs.remove(index);
                            msgs.add(index, sb.toString());
                        } else {
                            msgs.add("");
                            index++;
                        }
                    }
                }
            }

            index = 0;

            for (String msg : msgs) {
                if (Objects.equals(msg, "")) continue;

                if (index == 0 && listener.getFlags().containsKey("live") && listener.getFlags().get("live").contains("announce")) {
                    msg = "/announce " + listener.getIcons().getOrDefault("live", "") + " " + msg + " " + listener.getIcons().getOrDefault("live", "");
                } else {
                    msg = listener.getIcons().getOrDefault("live", "") + " " + msg + " " + listener.getIcons().getOrDefault("live", "");
                }

                bot.getClient().getChat().sendMessage(
                        revLinks.get(target.getAliasId()),
                        msg
                );
                index++;
            }
        }
    }
    public static void goOfflineEvent(ChannelGoOfflineEvent e) {
        Map<String, String> revLinks = Huinyabot.getInstance().getTargetLinks()
                .entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getValue,
                                Map.Entry::getKey,
                                (l, r) -> l,
                                LinkedHashMap::new
                        )
                );


        for (TargetModel target : Huinyabot.getInstance().getTargetCtrl().getAll().values()
                .stream()
                .filter(t->t.getListeners().containsKey(e.getChannel().getId()) && t.getListeners().get(e.getChannel().getId()).getEvents().contains("offline"))
                .collect(Collectors.toList())
        ) {
            NotifyListener listener = target.getListeners().get(e.getChannel().getId());

            ArrayList<String> msgs = new ArrayList<>();
            int index = 1;
            msgs.add(listener.getMessages().getOrDefault(
                    "offline",
                    bot.getLocale().literalText(
                            target.getLanguage(),
                            LineIds.WENT_OFFLINE_NOTIFICATION
                    )
            ).replaceAll(
                    "%\\{name}",
                    e.getChannel().getName()
            ));
            msgs.add("");

            if (listener.getFlags().containsKey("offline") && listener.getFlags().get("offline").contains("massping")) {
                List<String> chatters = bot.getClient().getMessagingInterface().getChatters(
                        revLinks.get(target.getAliasId())
                ).execute().getAllViewers();

                for (String chatter : chatters) {
                    StringBuilder sb = new StringBuilder();

                    if (
                            (
                                    listener.getIcons().getOrDefault("offline", "") +
                                            " " +
                                            msgs.get(index) +
                                            "@" +
                                            chatter +
                                            " " +
                                            listener.getIcons().getOrDefault("offline", "")
                            ).length() < 500
                    ) {
                        sb
                                .append(msgs.get(index))
                                .append("@")
                                .append(chatter)
                                .append(" ");

                        msgs.remove(index);
                        msgs.add(index, sb.toString());
                    } else {
                        msgs.add("");
                        index++;
                    }
                }
            } else {
                List<User> subUsers = bot.getClient().getHelix().getUsers(
                        bot.getProperties().getProperty("ACCESS_TOKEN", null),
                        listener.getSubscribers().stream().map(NotifySubscriber::getAliasId).collect(Collectors.toList()),
                        null
                ).execute().getUsers();

                for (NotifySubscriber subscriber : listener.getSubscribers()
                        .stream().filter(sub->sub.getSubscribedEvents().contains("offline")).collect(Collectors.toList())) {
                    User user = subUsers
                            .stream()
                            .filter(sub->Objects.equals(sub.getId(), subscriber.getAliasId()))
                            .findFirst().orElse(null);

                    if (user != null) {
                        StringBuilder sb = new StringBuilder();

                        if (
                                (
                                        listener.getIcons().getOrDefault("offline", "") +
                                                " " +
                                                msgs.get(index) +
                                                "@" +
                                                user.getLogin() +
                                                " " +
                                                listener.getIcons().getOrDefault("offline", "")
                                ).length() < 500
                        ) {
                            sb
                                    .append(msgs.get(index))
                                    .append("@")
                                    .append(user.getLogin())
                                    .append(" ");

                            msgs.remove(index);
                            msgs.add(index, sb.toString());
                        } else {
                            msgs.add("");
                            index++;
                        }
                    }
                }
            }

            index = 0;

            for (String msg : msgs) {
                if (Objects.equals(msg, "")) continue;

                if (index == 0 && listener.getFlags().containsKey("offline") && listener.getFlags().get("offline").contains("announce")) {
                    msg = "/announce " + listener.getIcons().getOrDefault("offline", "") + " " + msg + " " + listener.getIcons().getOrDefault("offline", "");
                } else {
                    msg = listener.getIcons().getOrDefault("offline", "") + " " + msg + " " + listener.getIcons().getOrDefault("offline", "");
                }

                bot.getClient().getChat().sendMessage(
                        revLinks.get(target.getAliasId()),
                        msg
                );
                index++;
            }
        }
    }
    public static void changeTitleEvent(ChannelChangeTitleEvent e) {
        Map<String, String> revLinks = Huinyabot.getInstance().getTargetLinks()
                .entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getValue,
                                Map.Entry::getKey,
                                (l, r) -> l,
                                LinkedHashMap::new
                        )
                );


        for (TargetModel target : Huinyabot.getInstance().getTargetCtrl().getAll().values()
                .stream()
                .filter(t->t.getListeners().containsKey(e.getChannel().getId()) && t.getListeners().get(e.getChannel().getId()).getEvents().contains("title"))
                .collect(Collectors.toList())
        ) {
            NotifyListener listener = target.getListeners().get(e.getStream().getUserId());

            ArrayList<String> msgs = new ArrayList<>();
            int index = 1;
            msgs.add(listener.getMessages().getOrDefault(
                    "title",
                    bot.getLocale().literalText(
                            target.getLanguage(),
                            LineIds.TITLE_CHANGE_NOTIFICATION
                    )
            ).replaceAll(
                    "%\\{name}",
                    e.getChannel().getName()
            ).replaceAll(
                    "%\\{title}",
                    (Objects.equals(e.getStream().getTitle(), "")) ? "N/A" : e.getStream().getTitle()
            ));
            msgs.add("");

            if (listener.getFlags().containsKey("title") && listener.getFlags().get("title").contains("massping")) {
                List<String> chatters = bot.getClient().getMessagingInterface().getChatters(
                        revLinks.get(target.getAliasId())
                ).execute().getAllViewers();

                for (String chatter : chatters) {
                    StringBuilder sb = new StringBuilder();

                    if (
                            (
                                    listener.getIcons().getOrDefault("title", "") +
                                            " " +
                                            msgs.get(index) +
                                            "@" +
                                            chatter +
                                            " " +
                                            listener.getIcons().getOrDefault("title", "")
                            ).length() < 500
                    ) {
                        sb
                                .append(msgs.get(index))
                                .append("@")
                                .append(chatter)
                                .append(" ");

                        msgs.remove(index);
                        msgs.add(index, sb.toString());
                    } else {
                        msgs.add("");
                        index++;
                    }
                }
            } else {
                List<User> subUsers = bot.getClient().getHelix().getUsers(
                        bot.getProperties().getProperty("ACCESS_TOKEN", null),
                        listener.getSubscribers().stream().map(NotifySubscriber::getAliasId).collect(Collectors.toList()),
                        null
                ).execute().getUsers();

                for (NotifySubscriber subscriber : listener.getSubscribers()
                        .stream().filter(sub->sub.getSubscribedEvents().contains("title")).collect(Collectors.toList())) {
                    User user = subUsers
                            .stream()
                            .filter(sub->Objects.equals(sub.getId(), subscriber.getAliasId()))
                            .findFirst().orElse(null);

                    if (user != null) {
                        StringBuilder sb = new StringBuilder();

                        if (
                                (
                                        listener.getIcons().getOrDefault("title", "") +
                                                " " +
                                                msgs.get(index) +
                                                "@" +
                                                user.getLogin() +
                                                " " +
                                                listener.getIcons().getOrDefault("title", "")
                                ).length() < 500
                        ) {
                            sb
                                    .append(msgs.get(index))
                                    .append("@")
                                    .append(user.getLogin())
                                    .append(" ");

                            msgs.remove(index);
                            msgs.add(index, sb.toString());
                        } else {
                            msgs.add("");
                            index++;
                        }
                    }
                }
            }

            index = 0;

            for (String msg : msgs) {
                if (Objects.equals(msg, "")) continue;

                if (index == 0 && listener.getFlags().containsKey("title") && listener.getFlags().get("title").contains("announce")) {
                    msg = "/announce " + listener.getIcons().getOrDefault("title", "") + " " + msg + " " + listener.getIcons().getOrDefault("title", "");
                } else {
                    msg = listener.getIcons().getOrDefault("title", "") + " " + msg + " " + listener.getIcons().getOrDefault("title", "");
                }

                bot.getClient().getChat().sendMessage(
                        revLinks.get(target.getAliasId()),
                        msg
                );
                index++;
            }
        }
    }
    public static void changeGameEvent(ChannelChangeGameEvent e) {
        Map<String, String> revLinks = Huinyabot.getInstance().getTargetLinks()
                .entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getValue,
                                Map.Entry::getKey,
                                (l, r) -> l,
                                LinkedHashMap::new
                        )
                );


        for (TargetModel target : Huinyabot.getInstance().getTargetCtrl().getAll().values()
                .stream()
                .filter(t->t.getListeners().containsKey(e.getChannel().getId()) && t.getListeners().get(e.getChannel().getId()).getEvents().contains("game"))
                .collect(Collectors.toList())
        ) {
            NotifyListener listener = target.getListeners().get(e.getChannel().getId());

            ArrayList<String> msgs = new ArrayList<>();
            int index = 1;
            msgs.add(listener.getMessages().getOrDefault(
                    "game",
                    bot.getLocale().literalText(
                            target.getLanguage(),
                            LineIds.GAME_CHANGE_NOTIFICATION
                    )
            ).replaceAll(
                    "%\\{name}",
                    e.getChannel().getName()
            ).replaceAll(
                    "%\\{game}",
                    (Objects.equals(e.getStream().getGameName(), "")) ? "N/A" : e.getStream().getGameName()
            ));
            msgs.add("");

            if (listener.getFlags().containsKey("game") && listener.getFlags().get("game").contains("massping")) {
                List<String> chatters = bot.getClient().getMessagingInterface().getChatters(
                        revLinks.get(target.getAliasId())
                ).execute().getAllViewers();

                for (String chatter : chatters) {
                    StringBuilder sb = new StringBuilder();

                    if (
                            (
                                    listener.getIcons().getOrDefault("game", "") +
                                            " " +
                                            msgs.get(index) +
                                            "@" +
                                            chatter +
                                            " " +
                                            listener.getIcons().getOrDefault("game", "")
                            ).length() < 500
                    ) {
                        sb
                                .append(msgs.get(index))
                                .append("@")
                                .append(chatter)
                                .append(" ");

                        msgs.remove(index);
                        msgs.add(index, sb.toString());
                    } else {
                        msgs.add("");
                        index++;
                    }
                }
            } else {
                List<User> subUsers = bot.getClient().getHelix().getUsers(
                        bot.getProperties().getProperty("ACCESS_TOKEN", null),
                        listener.getSubscribers().stream().map(NotifySubscriber::getAliasId).collect(Collectors.toList()),
                        null
                ).execute().getUsers();

                for (NotifySubscriber subscriber : listener.getSubscribers()
                        .stream().filter(sub->sub.getSubscribedEvents().contains("game")).collect(Collectors.toList())) {
                    User user = subUsers
                            .stream()
                            .filter(sub->Objects.equals(sub.getId(), subscriber.getAliasId()))
                            .findFirst().orElse(null);

                    if (user != null) {
                        StringBuilder sb = new StringBuilder();

                        if (
                                (
                                        listener.getIcons().getOrDefault("game", "") +
                                                " " +
                                                msgs.get(index) +
                                                "@" +
                                                user.getLogin() +
                                                " " +
                                                listener.getIcons().getOrDefault("game", "")
                                ).length() < 500
                        ) {
                            sb
                                    .append(msgs.get(index))
                                    .append("@")
                                    .append(user.getLogin())
                                    .append(" ");

                            msgs.remove(index);
                            msgs.add(index, sb.toString());
                        } else {
                            msgs.add("");
                            index++;
                        }
                    }
                }
            }

            index = 0;

            for (String msg : msgs) {
                if (Objects.equals(msg, "")) continue;

                if (index == 0 && listener.getFlags().containsKey("game") && listener.getFlags().get("game").contains("announce")) {
                    msg = "/announce " + listener.getIcons().getOrDefault("game", "") + " " + msg + " " + listener.getIcons().getOrDefault("game", "");
                } else {
                    msg = listener.getIcons().getOrDefault("game", "") + " " + msg + " " + listener.getIcons().getOrDefault("game", "");
                }

                bot.getClient().getChat().sendMessage(
                        revLinks.get(target.getAliasId()),
                        msg
                );
                index++;
            }
        }
    }
}
