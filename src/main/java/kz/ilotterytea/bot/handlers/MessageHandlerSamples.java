package kz.ilotterytea.bot.handlers;

import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import com.github.twitch4j.events.ChannelChangeGameEvent;
import com.github.twitch4j.events.ChannelChangeTitleEvent;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.events.ChannelGoOfflineEvent;
import com.github.twitch4j.helix.domain.Chatter;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.SharedConstants;
import kz.ilotterytea.bot.entities.CustomCommand;
import kz.ilotterytea.bot.entities.channels.Channel;
import kz.ilotterytea.bot.entities.channels.ChannelPreferences;
import kz.ilotterytea.bot.entities.listenables.Listenable;
import kz.ilotterytea.bot.entities.listenables.ListenableFlag;
import kz.ilotterytea.bot.entities.permissions.Permission;
import kz.ilotterytea.bot.entities.permissions.UserPermission;
import kz.ilotterytea.bot.entities.subscribers.Subscriber;
import kz.ilotterytea.bot.entities.subscribers.SubscriberEvent;
import kz.ilotterytea.bot.entities.users.UserPreferences;
import kz.ilotterytea.bot.i18n.LineIds;
import kz.ilotterytea.bot.utils.HibernateUtil;
import kz.ilotterytea.bot.utils.ParsedMessage;
import okhttp3.*;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The samples for Twitch4j events
 * @author ilotterytea
 * @since 1.0
 */
public class MessageHandlerSamples {
    private static final Logger LOG = LoggerFactory.getLogger(MessageHandlerSamples.class.getName());
    private static final Huinyabot bot = Huinyabot.getInstance();

    /**
     * Message handler sample for IRC message events.
     * @author ilotterytea
     * @since 1.0
     */
    public static void ircMessageEvent(IRCMessageEvent e) {
        if (e.getMessage().isEmpty()) {
            return;
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        session.getTransaction().begin();

        // Getting the channel info:
        List<Channel> channels = session.createQuery("from Channel where aliasId = :aliasId AND optOutTimestamp is null", Channel.class)
                .setParameter("aliasId", e.getChannel().getId())
                .getResultList();

        Channel channel;

        if (channels.isEmpty()) {
            LOG.warn("No channel for alias ID " + e.getChannel().getId() + "! Creating a new one...");

            channel = new Channel(Integer.parseInt(e.getChannel().getId()), e.getChannel().getName());
            ChannelPreferences preferences = new ChannelPreferences(channel);
            channel.setPreferences(preferences);

            session.persist(channel);
            session.persist(preferences);
        } else {
            channel = channels.get(0);
        }

        // Getting the user info:
        List<kz.ilotterytea.bot.entities.users.User> users = session.createQuery("from User where aliasId = :aliasId AND optOutTimestamp is null", kz.ilotterytea.bot.entities.users.User.class)
                .setParameter("aliasId", e.getUser().getId())
                .getResultList();

        kz.ilotterytea.bot.entities.users.User user;

        if (users.isEmpty()) {
            LOG.warn("No user for alias ID " + e.getUser().getId() + "! Creating a new one...");

            user = new kz.ilotterytea.bot.entities.users.User(Integer.parseInt(e.getUser().getId()), e.getUser().getName());
            UserPreferences preferences = new UserPreferences(user);
            user.setPreferences(preferences);

            user.setGlobalPermission(Permission.USER);

            UserPermission userPermission = new UserPermission();
            userPermission.setPermission(Permission.USER);
            channel.addPermission(userPermission);
            user.addPermission(userPermission);

            session.persist(user);
            session.persist(preferences);
        } else {
            user = users.get(0);
        }

        if (user.getGlobalPermission().getValue() == Permission.SUSPENDED.getValue()) {
            return;
        }

        // Update user's permissions:
        UserPermission userPermission = user.getPermissions()
                .stream()
                .filter(p -> p.getChannel().getAliasId().equals(channel.getAliasId()))
                .findFirst()
                .orElseGet(() -> {
                    UserPermission permission1 = new UserPermission();
                    permission1.setPermission(Permission.USER);
                    channel.addPermission(permission1);
                    user.addPermission(permission1);

                    return permission1;
                });

        if (userPermission.getPermission().getValue() == Permission.SUSPENDED.getValue()) {
            return;
        }

        if (Objects.equals(e.getChannel().getId(), e.getUser().getId())) {
            userPermission.setPermission(Permission.BROADCASTER);
        } else if (e.getBadges().containsKey("moderator")) {
            userPermission.setPermission(Permission.MOD);
        } else if (e.getBadges().containsKey("vip")) {
            userPermission.setPermission(Permission.VIP);
        } else {
            userPermission.setPermission(Permission.USER);
        }

        session.persist(userPermission);

        String MSG = e.getMessage().get();
        session.getTransaction().commit();
        
        final Optional<ParsedMessage> parsedMessage = ParsedMessage.parse(MSG, channel.getPreferences().getPrefix());

        // Processing the command:
        if (parsedMessage.isPresent()) {
        	Optional<String> response = bot.getLoader().call(
        			parsedMessage.get().getCommandId(),
            		e,
            		parsedMessage.get(),
            		channel,
            		user,
            		userPermission
            );
        	
        	if (response.isPresent()) {
                bot.getClient().getChat().sendMessage(
                        e.getChannel().getName(),
                        response.get(),
                        null,
                        (e.getMessageId().isEmpty()) ? null : e.getMessageId().get()
                );
            }
        	
        	return;
        }

        // Processing the custom commands:
        List<CustomCommand> commands = session.createQuery("from CustomCommand where channel = :channel AND name = :name AND isEnabled = true", CustomCommand.class)
                .setParameter("channel", channel)
                .setParameter("name", MSG)
                .getResultList();

        if (!commands.isEmpty()) {
            for (CustomCommand command : commands) {
                bot.getClient().getChat().sendMessage(
                        e.getChannel().getName(),
                        command.getMessage()
                );
            }
        }

        session.close();
    }

    public static void goLiveEvent(ChannelGoLiveEvent e) {
        // Getting channels that listen to a channel from an event:
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Listenable> listenables = session.createQuery("from Listenable where aliasId = :aliasId AND isEnabled = true", Listenable.class)
                .setParameter("aliasId", e.getChannel().getId())
                .getResultList();

        for (Listenable listenable : listenables) {
            Channel channel = listenable.getChannel();
            List<String> messages = new ArrayList<>();

            String icon = (listenable.getIcons().getLiveIcon() == null) ? "" : listenable.getIcons().getLiveIcon();

            String announcementLine;
            if (listenable.getMessages().getLiveMessage() == null) {
                announcementLine = bot.getLocale().literalText(
                        channel.getPreferences().getLanguage(),
                        LineIds.WENT_LIVE_NOTIFICATION
                );
            } else {
                announcementLine = listenable.getMessages().getLiveMessage();
            }

            messages.add(icon + " " + announcementLine
                    .replace("%{name}", e.getChannel().getName())
                    .replace("%{title}", (Objects.equals(e.getStream().getTitle(), "")) ? "N/A" : e.getStream().getTitle())
                    .replace("%{game}", (Objects.equals(e.getStream().getGameName(), "")) ? "N/A" : e.getStream().getGameName())
                    + " " + icon
            );

            List<String> currentMessage = new ArrayList<>();
            List<Subscriber> subscribers = listenable.getSubscribers().stream().filter(s -> s.getEnabled() && s.getEvents().contains(SubscriberEvent.LIVE)).collect(Collectors.toList());
            List<String> names = subscribers.stream().map(s ->s.getUser().getAliasName()).collect(Collectors.toList());

            // "Massping" clause.
            // Subscribers will be ignored if the MASSPING flag is enabled.
            if (listenable.getFlags().contains(ListenableFlag.MASSPING)) {
                try {
                    List<Chatter> userList = bot.getClient().getHelix().getChatters(
                            bot.getCredential().getAccessToken(),
                            listenable.getChannel().getAliasId().toString(),
                            bot.getCredential().getUserId(),
                            1000,
                            null
                    ).execute().getChatters();

                    names = userList.stream().map(Chatter::getUserName).collect(Collectors.toList());
                } catch (Exception ex) {
                    LOG.error("Could not get a list of users. The list of subscribers will be used instead: " + ex);
                }
            }

            // Adding a name to the message:
            // The message will end up looking like "@username, @someone, ...".
            for (String name : names) {
                if (
                        (icon +
                                " " +
                                String.join(", ", currentMessage) +
                                "@" +
                                name +
                                ", " +
                                " " +
                                icon).length() < 500
                ) {
                    currentMessage.add("@" + name);
                } else {
                    messages.add(icon + " " + String.join(", ", currentMessage) + " " + icon);
                    currentMessage = new ArrayList<>();
                    currentMessage.add("@" + name);
                }
            }

            messages.add(icon + " " + String.join(", ", currentMessage) + " " + icon);

            // Sending messages:
            for (String message : messages) {
                bot.getClient().getChat().sendMessage(
                        channel.getAliasName(),
                        message
                );
            }
        }

        session.close();
    }
    public static void goOfflineEvent(ChannelGoOfflineEvent e) {
        // Getting channels that listen to a channel from an event:
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Listenable> listenables = session.createQuery("from Listenable where aliasId = :aliasId AND isEnabled = true", Listenable.class)
                .setParameter("aliasId", e.getChannel().getId())
                .getResultList();

        for (Listenable listenable : listenables) {
            Channel channel = listenable.getChannel();
            List<String> messages = new ArrayList<>();

            String icon = (listenable.getIcons().getOfflineIcon() == null) ? "" : listenable.getIcons().getOfflineIcon();

            String announcementLine;
            if (listenable.getMessages().getOfflineMessage() == null) {
                announcementLine = bot.getLocale().literalText(
                        channel.getPreferences().getLanguage(),
                        LineIds.WENT_OFFLINE_NOTIFICATION
                );
            } else {
                announcementLine = listenable.getMessages().getOfflineMessage();
            }

            messages.add(icon + " " + announcementLine
                    .replace("%{name}", e.getChannel().getName())
                    + " " + icon
            );

            List<String> currentMessage = new ArrayList<>();
            List<Subscriber> subscribers = listenable.getSubscribers().stream().filter(s -> s.getEnabled() && s.getEvents().contains(SubscriberEvent.OFFLINE)).collect(Collectors.toList());
            List<String> names = subscribers.stream().map(s -> s.getUser().getAliasName()).collect(Collectors.toList());

            // "Massping" clause.
            // Subscribers will be ignored if the MASSPING flag is enabled.
            if (listenable.getFlags().contains(ListenableFlag.MASSPING)) {
                try {
                    List<Chatter> userList = bot.getClient().getHelix().getChatters(
                            bot.getCredential().getAccessToken(),
                            listenable.getChannel().getAliasId().toString(),
                            bot.getCredential().getUserId(),
                            1000,
                            null
                    ).execute().getChatters();

                    names = userList.stream().map(Chatter::getUserName).collect(Collectors.toList());
                } catch (Exception ex) {
                    LOG.error("Could not get a list of users. The list of subscribers will be used instead: " + ex);
                }
            }

            // Adding a name to the message:
            // The message will end up looking like "@username, @someone, ...".
            for (String name : names) {
                if (
                        (icon +
                                " " +
                                String.join(", ", currentMessage) +
                                "@" +
                                name +
                                ", " +
                                " " +
                                icon).length() < 500
                ) {
                    currentMessage.add("@" + name);
                } else {
                    messages.add(icon + " " + String.join(", ", currentMessage) + " " + icon);
                    currentMessage = new ArrayList<>();
                    currentMessage.add("@" + name);
                }
            }

            messages.add(icon + " " + String.join(", ", currentMessage) + " " + icon);

            // Sending messages:
            for (String message : messages) {
                bot.getClient().getChat().sendMessage(
                        channel.getAliasName(),
                        message
                );
            }
        }

        session.close();
    }
    public static void changeTitleEvent(ChannelChangeTitleEvent e) {
        // Getting channels that listen to a channel from an event:
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Listenable> listenables = session.createQuery("from Listenable where aliasId = :aliasId AND isEnabled = true", Listenable.class)
                .setParameter("aliasId", e.getChannel().getId())
                .getResultList();

        for (Listenable listenable : listenables) {
            Channel channel = listenable.getChannel();
            List<String> messages = new ArrayList<>();

            String icon = (listenable.getIcons().getTitleIcon() == null) ? "" : listenable.getIcons().getTitleIcon();

            String announcementLine;
            if (listenable.getMessages().getTitleMessage() == null) {
                announcementLine = bot.getLocale().literalText(
                        channel.getPreferences().getLanguage(),
                        LineIds.TITLE_CHANGE_NOTIFICATION
                );
            } else {
                announcementLine = listenable.getMessages().getTitleMessage();
            }

            messages.add(icon + " " + announcementLine
                    .replace("%{name}", e.getChannel().getName())
                    .replace("%{title}", (Objects.equals(e.getStream().getTitle(), "")) ? "N/A" : e.getStream().getTitle())
                    .replace("%{game}", (Objects.equals(e.getStream().getGameName(), "")) ? "N/A" : e.getStream().getGameName())
                    + " " + icon
            );

            List<String> currentMessage = new ArrayList<>();
            List<Subscriber> subscribers = listenable.getSubscribers().stream().filter(s -> s.getEnabled() && s.getEvents().contains(SubscriberEvent.TITLE)).collect(Collectors.toList());
            List<String> names = subscribers.stream().map(s ->s.getUser().getAliasName()).collect(Collectors.toList());

            // "Massping" clause.
            // Subscribers will be ignored if the MASSPING flag is enabled.
            if (listenable.getFlags().contains(ListenableFlag.MASSPING)) {
                try {
                    List<Chatter> userList = bot.getClient().getHelix().getChatters(
                            bot.getCredential().getAccessToken(),
                            listenable.getChannel().getAliasId().toString(),
                            bot.getCredential().getUserId(),
                            1000,
                            null
                    ).execute().getChatters();

                    names = userList.stream().map(Chatter::getUserName).collect(Collectors.toList());
                } catch (Exception ex) {
                    LOG.error("Could not get a list of users. The list of subscribers will be used instead: " + ex);
                }
            }

            // Adding a name to the message:
            // The message will end up looking like "@username, @someone, ...".
            for (String name : names) {
                if (
                        (icon +
                                " " +
                                String.join(", ", currentMessage) +
                                "@" +
                                name +
                                ", " +
                                " " +
                                icon).length() < 500
                ) {
                    currentMessage.add("@" + name);
                } else {
                    messages.add(icon + " " + String.join(", ", currentMessage) + " " + icon);
                    currentMessage = new ArrayList<>();
                    currentMessage.add("@" + name);
                }
            }

            messages.add(icon + " " + String.join(", ", currentMessage) + " " + icon);

            // Sending messages:
            for (String message : messages) {
                bot.getClient().getChat().sendMessage(
                        channel.getAliasName(),
                        message
                );
            }
        }

        session.close();
    }
    public static void changeGameEvent(ChannelChangeGameEvent e) {
        // Getting channels that listen to a channel from an event:
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Listenable> listenables = session.createQuery("from Listenable where aliasId = :aliasId AND isEnabled = true", Listenable.class)
                .setParameter("aliasId", e.getChannel().getId())
                .getResultList();

        for (Listenable listenable : listenables) {
            Channel channel = listenable.getChannel();
            List<String> messages = new ArrayList<>();

            String icon = (listenable.getIcons().getCategoryIcon() == null) ? "" : listenable.getIcons().getCategoryIcon();

            String announcementLine;
            if (listenable.getMessages().getCategoryMessage() == null) {
                announcementLine = bot.getLocale().literalText(
                        channel.getPreferences().getLanguage(),
                        LineIds.GAME_CHANGE_NOTIFICATION
                );
            } else {
                announcementLine = listenable.getMessages().getCategoryMessage();
            }

            messages.add(icon + " " + announcementLine
                    .replace("%{name}", e.getChannel().getName())
                    .replace("%{title}", (Objects.equals(e.getStream().getTitle(), "")) ? "N/A" : e.getStream().getTitle())
                    .replace("%{game}", (Objects.equals(e.getStream().getGameName(), "")) ? "N/A" : e.getStream().getGameName())
                    + " " + icon
            );

            List<String> currentMessage = new ArrayList<>();
            List<Subscriber> subscribers = listenable.getSubscribers().stream().filter(s -> s.getEnabled() && s.getEvents().contains(SubscriberEvent.TITLE)).collect(Collectors.toList());
            List<String> names = subscribers.stream().map(s ->s.getUser().getAliasName()).collect(Collectors.toList());

            // "Massping" clause.
            // Subscribers will be ignored if the MASSPING flag is enabled.
            if (listenable.getFlags().contains(ListenableFlag.MASSPING)) {
                try {
                    List<Chatter> userList = bot.getClient().getHelix().getChatters(
                            bot.getCredential().getAccessToken(),
                            listenable.getChannel().getAliasId().toString(),
                            bot.getCredential().getUserId(),
                            1000,
                            null
                    ).execute().getChatters();

                    names = userList.stream().map(Chatter::getUserName).collect(Collectors.toList());
                } catch (Exception ex) {
                    LOG.error("Could not get a list of users. The list of subscribers will be used instead: " + ex);
                }
            }

            // Adding a name to the message:
            // The message will end up looking like "@username, @someone, ...".
            for (String name : names) {
                if (
                        (icon +
                                " " +
                                String.join(", ", currentMessage) +
                                "@" +
                                name +
                                ", " +
                                " " +
                                icon).length() < 500
                ) {
                    currentMessage.add("@" + name);
                } else {
                    messages.add(icon + " " + String.join(", ", currentMessage) + " " + icon);
                    currentMessage = new ArrayList<>();
                    currentMessage.add("@" + name);
                }
            }

            messages.add(icon + " " + String.join(", ", currentMessage) + " " + icon);

            // Sending messages:
            for (String message : messages) {
                bot.getClient().getChat().sendMessage(
                        channel.getAliasName(),
                        message
                );
            }
        }

        session.close();
    }
}
