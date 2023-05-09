package kz.ilotterytea.bot.builtin;

import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import com.github.twitch4j.helix.domain.User;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.entities.channels.Channel;
import kz.ilotterytea.bot.entities.listenables.Listenable;
import kz.ilotterytea.bot.entities.listenables.ListenableFlag;
import kz.ilotterytea.bot.entities.listenables.ListenableIcons;
import kz.ilotterytea.bot.entities.listenables.ListenableMessages;
import kz.ilotterytea.bot.entities.permissions.Permission;
import kz.ilotterytea.bot.entities.permissions.UserPermission;
import kz.ilotterytea.bot.entities.subscribers.Subscriber;
import kz.ilotterytea.bot.entities.subscribers.SubscriberEvent;
import kz.ilotterytea.bot.i18n.LineIds;
import kz.ilotterytea.bot.utils.HibernateUtil;
import kz.ilotterytea.bot.utils.ParsedMessage;

import org.hibernate.Session;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Notify command.
 * @author ilotterytea
 * @since 1.3
 */
public class NotifyMeCommand implements Command {
    @Override
    public String getNameId() { return "notify"; }

    @Override
    public int getDelay() { return 10000; }

    @Override
    public Permission getPermissions() { return Permission.USER; }

    @Override
    public List<String> getOptions() { return List.of("massping", "clear", "no-massping", "no-sub", "announce"); }

    @Override
    public List<String> getSubcommands() { return List.of("subscribe", "unsubscribe", "list", "on", "off", "message", "flag", "unflag", "subs", "subscriptions", "sub", "unsub", "icon"); }

    @Override
    public List<String> getAliases() { return Collections.emptyList(); }

    @Override
    public Optional<String> run(IRCMessageEvent event, ParsedMessage message, Channel channel, kz.ilotterytea.bot.entities.users.User user, UserPermission permission) {
        if (message.getSubcommandId().isEmpty()) {
            return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
            		channel.getPreferences().getLanguage(),
            		LineIds.NO_SUBCMD
            ));
        }

        Huinyabot bot = Huinyabot.getInstance();

        if (message.getMessage().isEmpty()) {
        	return Optional.ofNullable(bot.getLocale().literalText(
                    channel.getPreferences().getLanguage(),
                    LineIds.NOT_ENOUGH_ARGS
            ));
        }
        ArrayList<String> s = new ArrayList<>(List.of(message.getMessage().get().split(" ")));

        String[] targetEvent = s.get(0).split(":");
        s.remove(0);

        // Getting the Twitch info about the target (specified user):
        List<User> twitchUsers;

        try {
            twitchUsers = bot.getClient().getHelix()
                    .getUsers(
                            bot.getCredential().getAccessToken(),
                            null,
                            Collections.singletonList(targetEvent[0])
                    )
                    .execute()
                    .getUsers();
        } catch (Exception e) {
            return null;
        }

        if (twitchUsers.isEmpty()) {
            return Optional.ofNullable(bot.getLocale().formattedText(
                    channel.getPreferences().getLanguage(),
                    LineIds.C_NOTIFY_USERNOTFOUND,
                    targetEvent[0]
            ));
        }

        User twitchUser = twitchUsers.get(0);

        // Getting active listenable for the target (specified user):
        Optional<Listenable> optionalListenable = channel.getListenables()
        		.stream()
        		.filter(l -> l.getAliasId().toString().equals(twitchUser.getId()))
        		.findFirst();
        
        Listenable listenable;

        Session session = HibernateUtil.getSessionFactory().openSession();
        if (
                permission.getPermission().getValue() >= Permission.BROADCASTER.getValue() &&
                        (!message.getSubcommandId().equals("sub") && !message.getSubcommandId().equals("unsub") &&
                        !message.getSubcommandId().equals("list") && !message.getSubcommandId().equals("subs")
                        )
        ) {
            session.getTransaction().begin();

            // Make the target listenable:
            if (message.getSubcommandId().equals("on")) {
                List<Listenable> otherListenables = session.createQuery("from Listenable where aliasId = :aliasId AND isEnabled = true AND channel != :channel", Listenable.class)
                        .setParameter("aliasId", twitchUser.getId())
                        .setParameter("channel", channel)
                        .getResultList();

                if (otherListenables.isEmpty()) {
                    bot.getClient().getClientHelper().enableStreamEventListener(twitchUser.getId(), twitchUser.getLogin());
                }

                if (channel.getListenables().stream().filter(c -> c.getAliasName().equals(targetEvent[0])).findFirst().isEmpty()) {
                    ListenableMessages messages = new ListenableMessages();
                    ListenableIcons icons = new ListenableIcons();

                    listenable = new Listenable(Integer.parseInt(twitchUser.getId()), twitchUser.getLogin(), messages, icons);
                    listenable.setIcons(icons);
                    listenable.setMessages(messages);

                    channel.addListenable(listenable);

                    session.persist(channel);
                    session.persist(listenable);
                    session.persist(messages);
                    session.persist(icons);

                    session.getTransaction().commit();
                    session.close();
                    return Optional.ofNullable(bot.getLocale().formattedText(
                            channel.getPreferences().getLanguage(),
                            LineIds.C_NOTIFY_SUCCESS_ON,
                            "all",
                            twitchUser.getLogin()
                    ));
                } else {
                    session.close();
                    return Optional.ofNullable(bot.getLocale().formattedText(
                            channel.getPreferences().getLanguage(),
                            LineIds.C_NOTIFY_ALREADYLISTENING,
                            twitchUser.getLogin()
                    ));
                }
            }

            if (optionalListenable.isEmpty()) {
                session.close();
                return Optional.ofNullable(bot.getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_NOTIFY_DOESNOTLISTENING,
                        targetEvent[0]
                ));
            } else {
                listenable = optionalListenable.get();
            }

            // Stop listening to the channel:
            if (message.getSubcommandId().get().equals("off")) {
                List<Listenable> otherListenables = session.createQuery("from Listenable where aliasId = :aliasId AND isEnabled = true AND channel != :channel", Listenable.class)
                        .setParameter("aliasId", twitchUser.getId())
                        .setParameter("channel", channel)
                        .getResultList();

                if (otherListenables.isEmpty()) {
                    bot.getClient().getClientHelper().disableStreamEventListenerForId(twitchUser.getId());
                }

                session.remove(listenable);
                session.getTransaction().commit();
                session.close();

                return Optional.ofNullable(bot.getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_NOTIFY_SUCCESS_OFFFULL,
                        "all",
                        twitchUser.getLogin()
                ));
            }

            String msg = String.join(" ", s);

            if (msg.isBlank()) {
                session.close();
                return Optional.ofNullable(bot.getLocale().literalText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_NOTIFY_NOMSG
                ));
            }

            // Setting the message:
            if (message.getSubcommandId().get().equals("message")) {
                ListenableMessages messages = listenable.getMessages();

                // Setting message for an event:
                if (targetEvent.length > 1) {
                    switch (targetEvent[1]) {
                        case "live":
                            messages.setLiveMessage(msg);
                            break;
                        case "offline":
                            messages.setOfflineMessage(msg);
                            break;
                        case "title":
                            messages.setTitleMessage(msg);
                            break;
                        case "category":
                            messages.setCategoryMessage(msg);
                            break;
                        default:
                            session.close();
                            return Optional.ofNullable(bot.getLocale().literalText(
                                    channel.getPreferences().getLanguage(),
                                    LineIds.C_NOTIFY_NOTHINGCHANGED
                            ));
                    }
                } else {
                    messages.setLiveMessage(msg);
                    messages.setOfflineMessage(msg);
                    messages.setTitleMessage(msg);
                    messages.setCategoryMessage(msg);
                }

                listenable.setMessages(messages);

                // Saving changes:
                session.persist(listenable);
                session.persist(messages);
                session.getTransaction().commit();
                session.close();

                if (targetEvent.length > 1) {
                    return Optional.ofNullable(bot.getLocale().formattedText(
                            channel.getPreferences().getLanguage(),
                            LineIds.C_NOTIFY_SUCCESS_COMMENT_UPDATED,
                            targetEvent[1],
                            channel.getAliasName()
                    ));
                } else {
                    return Optional.ofNullable(bot.getLocale().formattedText(
                            channel.getPreferences().getLanguage(),
                            LineIds.C_NOTIFY_SUCCESS_COMMENT_UPDATEDALL,
                            channel.getAliasName()
                    ));
                }
            }

            // Setting the icon:
            else if (message.getSubcommandId().get().equals("icon")) {
                ListenableIcons icons = listenable.getIcons();

                if (msg.isBlank()) {
                    session.close();
                    return Optional.ofNullable(bot.getLocale().literalText(
                            channel.getPreferences().getLanguage(),
                            LineIds.C_NOTIFY_NOMSG
                    ));
                }

                // Setting message for an event:
                if (targetEvent.length > 1) {
                    switch (targetEvent[1]) {
                        case "live":
                            icons.setLiveIcon(msg);
                            break;
                        case "offline":
                            icons.setOfflineIcon(msg);
                            break;
                        case "title":
                            icons.setTitleIcon(msg);
                            break;
                        case "category":
                            icons.setCategoryIcon(msg);
                            break;
                        default:
                            session.close();
                            return Optional.ofNullable(bot.getLocale().literalText(
                                    channel.getPreferences().getLanguage(),
                                    LineIds.C_NOTIFY_NOTHINGCHANGED
                            ));
                    }
                } else {
                    icons.setLiveIcon(msg);
                    icons.setOfflineIcon(msg);
                    icons.setTitleIcon(msg);
                    icons.setCategoryIcon(msg);
                }

                // Saving changes:
                session.getTransaction().begin();
                session.persist(icons);
                session.getTransaction().commit();
                session.close();

                if (targetEvent.length > 1) {
                    return Optional.ofNullable(bot.getLocale().formattedText(
                            channel.getPreferences().getLanguage(),
                            LineIds.C_NOTIFY_SUCCESS_ICON_UPDATED,
                            targetEvent[1],
                            listenable.getAliasName()
                    ));
                } else {
                    return Optional.ofNullable(bot.getLocale().formattedText(
                            channel.getPreferences().getLanguage(),
                            LineIds.C_NOTIFY_SUCCESS_ICON_UPDATEDALL,
                            listenable.getAliasName()
                    ));
                }
            }

            // Setting the flag:
            else if (message.getSubcommandId().get().equals("flag")) {
                ListenableFlag flag;

                // Parsing the flag name:
                switch (msg.toLowerCase()) {
                    case "massping":
                        flag = ListenableFlag.MASSPING;
                        break;
                    case "no-sub":
                    case "unsubscriberable":
                        flag = ListenableFlag.UNSUBSCRIBERABLE;
                        break;
                    default:
                        session.close();
                        return Optional.ofNullable(bot.getLocale().literalText(
                                channel.getPreferences().getLanguage(),
                                LineIds.C_NOTIFY_NOFLAG
                        ));
                }

                Set<ListenableFlag> flags = listenable.getFlags();
                String msgToSend;

                // Updating flags:
                if (flags.add(flag)) {
                    msgToSend = bot.getLocale().formattedText(
                            channel.getPreferences().getLanguage(),
                            LineIds.C_NOTIFY_SUCCESS_FLAG,
                            msg.toLowerCase(),
                            targetEvent[1],
                            listenable.getAliasName()
                    );
                } else {
                    msgToSend = bot.getLocale().literalText(
                            channel.getPreferences().getLanguage(),
                            LineIds.C_NOTIFY_NOTHINGCHANGED
                    );
                }

                listenable.setFlags(flags);

                // Saving changes:
                session.persist(listenable);
                session.getTransaction().commit();
                session.close();

                return Optional.ofNullable(msgToSend);
            }

            // Removing the flag:
            else if (message.getSubcommandId().get().equals("unflag")) {
                ListenableFlag flag;

                // Parsing the flag name:
                switch (msg.toLowerCase()) {
                    case "massping":
                        flag = ListenableFlag.MASSPING;
                        break;
                    case "no-sub":
                    case "unsubscriberable":
                        flag = ListenableFlag.UNSUBSCRIBERABLE;
                        break;
                    default:
                        session.close();
                        return Optional.ofNullable(bot.getLocale().literalText(
                                channel.getPreferences().getLanguage(),
                                LineIds.C_NOTIFY_NOFLAG
                        ));
                }

                Set<ListenableFlag> flags = listenable.getFlags();
                String msgToSend;

                // Updating flags:
                if (flags.remove(flag)) {
                    msgToSend = bot.getLocale().formattedText(
                            channel.getPreferences().getLanguage(),
                            LineIds.C_NOTIFY_SUCCESS_UNFLAG,
                            msg.toLowerCase(),
                            targetEvent[1],
                            channel.getAliasName()
                    );
                } else {
                    msgToSend = bot.getLocale().literalText(
                            channel.getPreferences().getLanguage(),
                            LineIds.C_NOTIFY_NOTHINGCHANGED
                    );
                }

                listenable.setFlags(flags);

                // Saving changes:
                session.persist(listenable);
                session.getTransaction().commit();
                session.close();

                return Optional.ofNullable(msgToSend);
            }
            else {
                session.close();
                return Optional.ofNullable(bot.getLocale().literalText(
                        channel.getPreferences().getLanguage(),
                        LineIds.NO_SUBCMD
                ));
            }
        }

        // Obtain available channels for subscription:
        if (message.getSubcommandId().get().equals("list")) {
            if (channel.getListenables().isEmpty()) {
                session.close();
                return Optional.ofNullable(bot.getLocale().literalText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_NOTIFY_NOLISTENINGCHANNELS
                ));
            }

            session.close();
            return Optional.ofNullable(bot.getLocale().formattedText(
                    channel.getPreferences().getLanguage(),
                    LineIds.C_NOTIFY_SUCCESS_LIST,
                    channel.getListenables().stream().map(Listenable::getAliasName).collect(Collectors.joining(", "))
            ));
        }

        // Receiving subscribed channels:
        else if (message.getSubcommandId().get().equals("subs")) {
            if (user.getSubscribers().isEmpty()) {
                session.close();
                return Optional.ofNullable(bot.getLocale().literalText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_NOTIFY_SUCCESS_SUBSNOONE
                ));
            }

            session.close();
            return Optional.ofNullable(bot.getLocale().formattedText(
                    channel.getPreferences().getLanguage(),
                    LineIds.C_NOTIFY_SUCCESS_LIST,
                    user.getSubscribers().stream().map(sub -> {
                        String events = sub.getEvents().stream().map(SubscriberEvent::getName).collect(Collectors.joining("-"));

                        return sub.getListenable().getAliasName() + " (" + events + ")";
                    }).collect(Collectors.joining(", "))
            ));

        }

        if (optionalListenable.isEmpty()) {
            session.close();
            return Optional.ofNullable(bot.getLocale().formattedText(
                    channel.getPreferences().getLanguage(),
                    LineIds.C_NOTIFY_DOESNOTLISTENING,
                    targetEvent[0]
            ));
        } else {
            listenable = optionalListenable.get();
        }

        // Subscribe to the channel:
        if (message.getSubcommandId().get().equals("sub")) {
            Subscriber subscriber = listenable.getSubscribers()
                    .stream()
                    .filter(p -> p.getUser().getAliasId().equals(user.getAliasId()))
                    .findFirst()
                    .orElseGet(() -> {
                        Subscriber sub = new Subscriber(user, listenable);

                        user.addSubscriber(sub);
                        listenable.addSubscriber(sub);

                        session.persist(user);
                        session.persist(listenable);
                        session.persist(sub);

                        return sub;
                    });

            Set<SubscriberEvent> events = subscriber.getEvents();
            List<String> eventIds = new ArrayList<>();

            if (targetEvent.length > 1) {
                switch (targetEvent[1]) {
                    case "live":
                        if (events.add(SubscriberEvent.LIVE)) {
                            eventIds.add(SubscriberEvent.LIVE.getName());
                        }
                        break;
                    case "offline":
                        if (events.add(SubscriberEvent.OFFLINE)) {
                            eventIds.add(SubscriberEvent.OFFLINE.getName());
                        }
                        break;
                    case "title":
                        if (events.add(SubscriberEvent.TITLE)) {
                            eventIds.add(SubscriberEvent.TITLE.getName());
                        }
                        break;
                    case "category":
                        if (events.add(SubscriberEvent.CATEGORY)) {
                            eventIds.add(SubscriberEvent.CATEGORY.getName());
                        }
                        break;
                    default:
                        session.close();
                        return Optional.ofNullable(bot.getLocale().literalText(
                                channel.getPreferences().getLanguage(),
                                LineIds.C_NOTIFY_NOTHINGCHANGED
                        ));
                }
            } else {
                if (events.add(SubscriberEvent.LIVE)) {
                    eventIds.add(SubscriberEvent.LIVE.getName());
                }

                if (events.add(SubscriberEvent.TITLE)) {
                    eventIds.add(SubscriberEvent.TITLE.getName());
                }

                if (events.add(SubscriberEvent.OFFLINE)) {
                    eventIds.add(SubscriberEvent.OFFLINE.getName());
                }

                if (events.add(SubscriberEvent.CATEGORY)) {
                    eventIds.add(SubscriberEvent.CATEGORY.getName());
                }
            }

            String msgToSend;

            if (eventIds.isEmpty()) {
                msgToSend = bot.getLocale().literalText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_NOTIFY_NOTHINGCHANGED
                );
            } else {
                msgToSend = bot.getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_NOTIFY_SUCCESS_SUB,
                        String.join(", ", eventIds),
                        listenable.getAliasName()
                );
            }

            session.persist(subscriber);

            session.getTransaction().commit();
            session.close();

            return Optional.ofNullable(msgToSend);
        }

        // Unsubscribe from the channel:
        else if (message.getSubcommandId().get().equals("unsub")) {
            List<Subscriber> subscribers = session.createQuery("from Subscriber where user = :user AND listenable = :listenable", Subscriber.class)
                    .setParameter("listenable", listenable)
                    .setParameter("user", user)
                    .getResultList();

            if (subscribers.isEmpty()) {
                session.close();
                return Optional.ofNullable(bot.getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_NOTIFY_NOTSUB,
                        listenable.getAliasName()
                ));
            }

            Subscriber subscriber = subscribers.get(0);

            Set<SubscriberEvent> events = subscriber.getEvents();
            List<String> eventIds = new ArrayList<>();

            if (targetEvent.length > 1) {
                switch (targetEvent[1]) {
                    case "live":
                        if (events.remove(SubscriberEvent.LIVE)) {
                            eventIds.add(SubscriberEvent.LIVE.getName());
                        }
                        break;
                    case "offline":
                        if (events.remove(SubscriberEvent.OFFLINE)) {
                            eventIds.add(SubscriberEvent.OFFLINE.getName());
                        }
                        break;
                    case "title":
                        if (events.remove(SubscriberEvent.TITLE)) {
                            eventIds.add(SubscriberEvent.TITLE.getName());
                        }
                        break;
                    case "category":
                        if (events.remove(SubscriberEvent.CATEGORY)) {
                            eventIds.add(SubscriberEvent.CATEGORY.getName());
                        }
                        break;
                    default:
                        session.close();
                        return Optional.ofNullable(bot.getLocale().literalText(
                                channel.getPreferences().getLanguage(),
                                LineIds.C_NOTIFY_NOTHINGCHANGED
                        ));
                }
            } else {
                if (events.remove(SubscriberEvent.LIVE)) {
                    eventIds.add(SubscriberEvent.LIVE.getName());
                }

                if (events.remove(SubscriberEvent.TITLE)) {
                    eventIds.add(SubscriberEvent.TITLE.getName());
                }

                if (events.remove(SubscriberEvent.OFFLINE)) {
                    eventIds.add(SubscriberEvent.OFFLINE.getName());
                }

                if (events.remove(SubscriberEvent.CATEGORY)) {
                    eventIds.add(SubscriberEvent.CATEGORY.getName());
                }
            }

            String msgToSend;

            if (eventIds.isEmpty()) {
                msgToSend = bot.getLocale().literalText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_NOTIFY_NOTHINGCHANGED
                );
            } else {
                msgToSend = bot.getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_NOTIFY_SUCCESS_UNSUB,
                        String.join(", ", eventIds),
                        listenable.getAliasName()
                );
            }

            if (subscriber.getEvents().isEmpty()) {
                session.remove(subscriber);
            } else {
                session.persist(subscriber);
            }

            session.getTransaction().commit();
            session.close();

            return Optional.ofNullable(msgToSend);
        }

        session.close();
        return Optional.empty();
    }
}
