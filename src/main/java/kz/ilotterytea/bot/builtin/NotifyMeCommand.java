package kz.ilotterytea.bot.builtin;

import com.github.twitch4j.helix.domain.User;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.api.permissions.Permissions;
import kz.ilotterytea.bot.entities.channels.Channel;
import kz.ilotterytea.bot.entities.listenables.Listenable;
import kz.ilotterytea.bot.entities.listenables.ListenableFlag;
import kz.ilotterytea.bot.entities.listenables.ListenableIcons;
import kz.ilotterytea.bot.entities.listenables.ListenableMessages;
import kz.ilotterytea.bot.entities.permissions.UserPermission;
import kz.ilotterytea.bot.entities.subscribers.Subscriber;
import kz.ilotterytea.bot.entities.subscribers.SubscriberEvent;
import kz.ilotterytea.bot.i18n.LineIds;
import kz.ilotterytea.bot.models.ArgumentsModel;
import kz.ilotterytea.bot.utils.HibernateUtil;
import org.hibernate.Session;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Notify command.
 * @author ilotterytea
 * @since 1.3
 */
public class NotifyMeCommand extends Command {
    @Override
    public String getNameId() { return "notify"; }

    @Override
    public int getDelay() { return 10000; }

    @Override
    public Permissions getPermissions() { return Permissions.USER; }

    @Override
    public ArrayList<String> getOptions() { return new ArrayList<>(Arrays.asList("massping", "clear", "no-massping", "no-sub", "announce")); }

    @Override
    public ArrayList<String> getSubcommands() { return new ArrayList<>(Arrays.asList("subscribe", "unsubscribe", "list", "on", "off", "message", "flag", "unflag", "subs", "subscriptions", "sub", "unsub", "icon")); }

    @Override
    public ArrayList<String> getAliases() { return new ArrayList<>(); }

    @Override
    public String run(ArgumentsModel m) {
        if (m.getMessage().getSubCommand() == null) {
            return null;
        }

        Session session = HibernateUtil.getSessionFactory().openSession();

        // Getting channel local info:
        List<Channel> channels = session.createQuery("from Channel where aliasId = :aliasId", Channel.class)
                .setParameter("aliasId", m.getEvent().getChannel().getId())
                .getResultList();

        Channel channel = channels.get(0);

        // Getting sender local info:
        List<kz.ilotterytea.bot.entities.users.User> users = session.createQuery("from User where aliasId = :aliasId", kz.ilotterytea.bot.entities.users.User.class)
                .setParameter("aliasId", m.getEvent().getUser().getId())
                .getResultList();

        kz.ilotterytea.bot.entities.users.User user = users.get(0);

        // Getting permission info:
        List<UserPermission> permissions = session.createQuery("from UserPermission where user = :user AND channel = :channel", UserPermission.class)
                .setParameter("user", user)
                .setParameter("channel", channel)
                .getResultList();

        UserPermission permission = permissions.get(0);

        Huinyabot bot = Huinyabot.getInstance();

        List<String> s = new ArrayList<>(List.of(m.getMessage().getMessage().split(" ")));

        if (s.isEmpty()) {
            session.close();
            return bot.getLocale().literalText(
                    channel.getPreferences().getLanguage(),
                    LineIds.NOT_ENOUGH_ARGS
            );
        }

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
            session.close();
            return null;
        }

        if (twitchUsers.isEmpty()) {
            session.close();
            return bot.getLocale().formattedText(
                    channel.getPreferences().getLanguage(),
                    LineIds.C_NOTIFY_USERNOTFOUND,
                    targetEvent[0]
            );
        }

        User twitchUser = twitchUsers.get(0);

        // Getting active listenables for the target (specified user):
        List<Listenable> listenables = session.createQuery("from Listenable where aliasId = :aliasId AND channel = :channel", Listenable.class)
                .setParameter("aliasId", twitchUser.getId())
                .setParameter("channel", channel)
                .getResultList();

        Listenable listenable;

        if (
                permission.getPermission().getValue() >= Permissions.BROADCASTER.getId() &&
                        (!m.getMessage().getSubCommand().equals("sub") && !m.getMessage().getSubCommand().equals("unsub") &&
                        !m.getMessage().getSubCommand().equals("list") && !m.getMessage().getSubCommand().equals("subs")
                        )
        ) {
            session.getTransaction().begin();

            // Make the target listenable:
            if (m.getMessage().getSubCommand().equals("on")) {
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
                    return bot.getLocale().formattedText(
                            channel.getPreferences().getLanguage(),
                            LineIds.C_NOTIFY_SUCCESS_ON,
                            "all",
                            twitchUser.getLogin()
                    );
                } else {
                    session.close();
                    return bot.getLocale().formattedText(
                            channel.getPreferences().getLanguage(),
                            LineIds.C_NOTIFY_ALREADYLISTENING,
                            twitchUser.getLogin()
                    );
                }
            }

            if (listenables.isEmpty()) {
                session.close();
                return bot.getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_NOTIFY_DOESNOTLISTENING,
                        targetEvent[0]
                );
            } else {
                listenable = listenables.get(0);
            }

            // Stop listening to the channel:
            if (m.getMessage().getSubCommand().equals("off")) {
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

                return bot.getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_NOTIFY_SUCCESS_OFFFULL,
                        "all",
                        twitchUser.getLogin()
                );
            }

            String msg = String.join(" ", s);

            if (msg.isBlank()) {
                session.close();
                return bot.getLocale().literalText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_NOTIFY_NOMSG
                );
            }

            // Setting the message:
            if (m.getMessage().getSubCommand().equals("message")) {
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
                            return bot.getLocale().literalText(
                                    channel.getPreferences().getLanguage(),
                                    LineIds.C_NOTIFY_NOTHINGCHANGED
                            );
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
                    return bot.getLocale().formattedText(
                            channel.getPreferences().getLanguage(),
                            LineIds.C_NOTIFY_SUCCESS_COMMENT_UPDATED,
                            targetEvent[1],
                            channel.getAliasName()
                    );
                } else {
                    return bot.getLocale().formattedText(
                            channel.getPreferences().getLanguage(),
                            LineIds.C_NOTIFY_SUCCESS_COMMENT_UPDATEDALL,
                            channel.getAliasName()
                    );
                }
            }

            // Setting the icon:
            else if (m.getMessage().getSubCommand().equals("icon")) {
                ListenableIcons icons = listenable.getIcons();

                if (msg.isBlank()) {
                    session.close();
                    return bot.getLocale().literalText(
                            channel.getPreferences().getLanguage(),
                            LineIds.C_NOTIFY_NOMSG
                    );
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
                            return bot.getLocale().literalText(
                                    channel.getPreferences().getLanguage(),
                                    LineIds.C_NOTIFY_NOTHINGCHANGED
                            );
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
                    return bot.getLocale().formattedText(
                            channel.getPreferences().getLanguage(),
                            LineIds.C_NOTIFY_SUCCESS_ICON_UPDATED,
                            targetEvent[1],
                            listenable.getAliasName()
                    );
                } else {
                    return bot.getLocale().formattedText(
                            channel.getPreferences().getLanguage(),
                            LineIds.C_NOTIFY_SUCCESS_ICON_UPDATEDALL,
                            listenable.getAliasName()
                    );
                }
            }

            // Setting the flag:
            else if (m.getMessage().getSubCommand().equals("flag")) {
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
                        return bot.getLocale().literalText(
                                channel.getPreferences().getLanguage(),
                                LineIds.C_NOTIFY_NOFLAG
                        );
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

                return msgToSend;
            }

            // Removing the flag:
            else if (m.getMessage().getSubCommand().equals("unflag")) {
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
                        return bot.getLocale().literalText(
                                channel.getPreferences().getLanguage(),
                                LineIds.C_NOTIFY_NOFLAG
                        );
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

                return msgToSend;
            }
            else {
                session.close();
                return bot.getLocale().literalText(
                        channel.getPreferences().getLanguage(),
                        LineIds.NO_SUBCMD
                );
            }
        }

        // Obtain available channels for subscription:
        if (m.getMessage().getSubCommand().equals("list")) {
            if (channel.getListenables().isEmpty()) {
                session.close();
                return bot.getLocale().literalText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_NOTIFY_NOLISTENINGCHANNELS
                );
            }

            session.close();
            return bot.getLocale().formattedText(
                    channel.getPreferences().getLanguage(),
                    LineIds.C_NOTIFY_SUCCESS_LIST,
                    channel.getListenables().stream().map(Listenable::getAliasName).collect(Collectors.joining(", "))
            );
        }

        // Receiving subscribed channels:
        else if (m.getMessage().getSubCommand().equals("subs")) {
            if (user.getSubscribers().isEmpty()) {
                session.close();
                return bot.getLocale().literalText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_NOTIFY_SUCCESS_SUBSNOONE
                );
            }

            session.close();
            return bot.getLocale().formattedText(
                    channel.getPreferences().getLanguage(),
                    LineIds.C_NOTIFY_SUCCESS_LIST,
                    user.getSubscribers().stream().map(sub -> {
                        String events = sub.getEvents().stream().map(SubscriberEvent::getName).collect(Collectors.joining("-"));

                        return sub.getListenable().getAliasName() + " (" + events + ")";
                    }).collect(Collectors.joining(", "))
            );

        }

        if (listenables.isEmpty()) {
            session.close();
            return bot.getLocale().formattedText(
                    channel.getPreferences().getLanguage(),
                    LineIds.C_NOTIFY_DOESNOTLISTENING,
                    targetEvent[0]
            );
        } else {
            listenable = listenables.get(0);
        }

        // Subscribe to the channel:
        if (m.getMessage().getSubCommand().equals("sub")) {
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
                        return bot.getLocale().literalText(
                                channel.getPreferences().getLanguage(),
                                LineIds.C_NOTIFY_NOTHINGCHANGED
                        );
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

            return msgToSend;
        }

        // Unsubscribe from the channel:
        else if (m.getMessage().getSubCommand().equals("unsub")) {
            List<Subscriber> subscribers = session.createQuery("from Subscriber where user = :user AND listenable = :listenable", Subscriber.class)
                    .setParameter("listenable", listenable)
                    .setParameter("user", user)
                    .getResultList();

            if (subscribers.isEmpty()) {
                session.close();
                return bot.getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_NOTIFY_NOTSUB,
                        listenable.getAliasName()
                );
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
                        return bot.getLocale().literalText(
                                channel.getPreferences().getLanguage(),
                                LineIds.C_NOTIFY_NOTHINGCHANGED
                        );
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

            return msgToSend;
        }

        session.close();
        return null;
    }
}
