package kz.ilotterytea.bot.handlers;

import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import com.github.twitch4j.events.ChannelChangeGameEvent;
import com.github.twitch4j.events.ChannelChangeTitleEvent;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.events.ChannelGoOfflineEvent;
import com.github.twitch4j.helix.domain.AnnouncementColor;
import com.github.twitch4j.helix.domain.Chatter;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.SharedConstants;
import kz.ilotterytea.bot.api.commands.Command;
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
import kz.ilotterytea.bot.models.MessageModel;
import kz.ilotterytea.bot.utils.HibernateUtil;
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

        String MSG = e.getMessage().get();

        final MessageModel messageModel = MessageModel.create(e.getMessage().get(), channel.getPreferences().getPrefix());

        // Update user's permissions:
        Optional<UserPermission> optionalUserPermission = user.getPermissions().stream().filter(p -> p.getChannel().getAliasId().equals(channel.getAliasId())).findFirst();

        if (optionalUserPermission.isPresent()) {
            UserPermission userPermission = optionalUserPermission.get();

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
        }

        // 'Test':
        if (Objects.equals(MSG, "test")) {
            bot.getClient().getChat().sendMessage(
                    e.getChannel().getName(),
                    "test has successfully completed!"
            );
            return;
        }

        session.getTransaction().commit();

        // Processing the command:
        if (MSG.startsWith(channel.getPreferences().getPrefix())) {
            Optional<Command> optionalCommand = bot.getLoader().getCommand(messageModel.getCommand());

            if (optionalCommand.isPresent()) {
                Command command = optionalCommand.get();

                String response = command.run(null);

                if (response != null) {
                    bot.getClient().getChat().sendMessage(
                            e.getChannel().getName(),
                            response,
                            null,
                            (e.getMessageId().isEmpty()) ? null : e.getMessageId().get()
                    );
                }

                return;
            }
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

        // Markov processing:
        if (markovMessagePattern.matcher(MSG).find()) {
            MSG = markovUsernamePattern.matcher(MSG).replaceAll("");
            MSG = markovURLPattern.matcher(MSG).replaceAll("");

            MSG = MSG.trim();

            OkHttpClient client = new OkHttpClient.Builder().build();
            String url = Objects.requireNonNull(
                        HttpUrl.parse(SharedConstants.NEUROBAJ_URL + "/api/v1/gen")
                    )
                    .newBuilder()
                    .addQueryParameter("message", MSG)
                    .addQueryParameter("nsfw", "0")
                    .build()
                    .toString();


            Request request = new Request.Builder()
                    .get()
                    .url(url)
                    .build();

            Call call = client.newCall(request);
            Response response;

            try {
                response = call.execute();
            } catch (IOException ex) {
                ex.printStackTrace();
                bot.getClient().getChat().sendMessage(
                        e.getChannel().getName(),
                        bot.getLocale().literalText(
                                channel.getPreferences().getLanguage(),
                                LineIds.SOMETHING_WENT_WRONG
                        ),
                        null,
                        (e.getMessageId().isPresent()) ? null : e.getMessageId().get()
                );
                return;
            }

            if (response.code() != 200) {
                bot.getClient().getChat().sendMessage(
                        e.getChannel().getName(),
                        bot.getLocale().formattedText(
                                channel.getPreferences().getLanguage(),
                                LineIds.HTTP_ERROR,
                                String.valueOf(response.code()),
                                "Neurobaj"
                        ),
                        null,
                        (e.getMessageId().isPresent()) ? null : e.getMessageId().get()
                );
                return;
            }

            String generatedText;

            try {
                assert response.body() != null;
                generatedText = response.body().string();
            } catch (IOException ex) {
                ex.printStackTrace();
                bot.getClient().getChat().sendMessage(
                        e.getChannel().getName(),
                        bot.getLocale().literalText(
                                channel.getPreferences().getLanguage(),
                                LineIds.SOMETHING_WENT_WRONG
                        ),
                        null,
                        (e.getMessageId().isPresent()) ? null : e.getMessageId().get()
                );
                return;
            }

            bot.getClient().getChat().sendMessage(
                    e.getChannel().getName(),
                    generatedText,
                    null,
                    (e.getMessageId().isEmpty()) ? null : e.getMessageId().get()
            );
        }
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

            messages.add(listenable.getMessages().getLiveMessage()
                    .replace("%\\{name}", e.getChannel().getName())
                    .replace("%\\{title}", (Objects.equals(e.getStream().getTitle(), "")) ? "N/A" : e.getStream().getTitle())
                    .replace("%\\{game}", (Objects.equals(e.getStream().getGameName(), "")) ? "N/A" : e.getStream().getGameName())
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
                        (listenable.getIcons().getLiveIcon() +
                                " " +
                                String.join(", ", currentMessage) +
                                "@" +
                                name +
                                ", " +
                                " " +
                                listenable.getIcons().getLiveIcon()).length() > 500
                ) {
                    currentMessage.add("@" + name);
                } else {
                    messages.add(listenable.getIcons().getLiveIcon() + " " + String.join(", ", currentMessage) + " " + listenable.getIcons().getLiveIcon());
                    currentMessage = new ArrayList<>();
                    currentMessage.add("@" + name);
                }
            }

            // Sending messages:
            for (int i = 0; i < messages.size(); i++) {
                if (i == 0) {
                    try {
                        bot.getClient().getHelix().sendChatAnnouncement(
                                bot.getCredential().getAccessToken(),
                                listenable.getChannel().getAliasId().toString(),
                                bot.getCredential().getUserId(),
                                messages.get(i),
                                AnnouncementColor.PRIMARY
                        );
                    } catch (Exception ex) {
                        LOG.error("Couldn't send an announcement message: " + ex);

                        bot.getClient().getChat().sendMessage(
                                channel.getAliasName(),
                                messages.get(i)
                        );
                    }
                } else {
                    bot.getClient().getChat().sendMessage(
                            channel.getAliasName(),
                            messages.get(i)
                    );
                }
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

            messages.add(listenable.getMessages().getOfflineMessage()
                    .replace("%\\{name}", e.getChannel().getName())
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
                        (listenable.getIcons().getOfflineIcon() +
                                " " +
                                String.join(", ", currentMessage) +
                                "@" +
                                name +
                                ", " +
                                " " +
                                listenable.getIcons().getOfflineIcon()).length() > 500
                ) {
                    currentMessage.add("@" + name);
                } else {
                    messages.add(listenable.getIcons().getOfflineIcon() + " " + String.join(", ", currentMessage) + " " + listenable.getIcons().getOfflineIcon());
                    currentMessage = new ArrayList<>();
                    currentMessage.add("@" + name);
                }
            }

            // Sending messages:
            for (int i = 0; i < messages.size(); i++) {
                if (i == 0) {
                    try {
                        bot.getClient().getHelix().sendChatAnnouncement(
                                bot.getCredential().getAccessToken(),
                                listenable.getChannel().getAliasId().toString(),
                                bot.getCredential().getUserId(),
                                messages.get(i),
                                AnnouncementColor.PRIMARY
                        );
                    } catch (Exception ex) {
                        LOG.error("Couldn't send an announcement message: " + ex);

                        bot.getClient().getChat().sendMessage(
                                channel.getAliasName(),
                                messages.get(i)
                        );
                    }
                } else {
                    bot.getClient().getChat().sendMessage(
                            channel.getAliasName(),
                            messages.get(i)
                    );
                }
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

            messages.add(listenable.getMessages().getTitleMessage()
                    .replace("%\\{name}", e.getChannel().getName())
                    .replace("%\\{title}", (Objects.equals(e.getStream().getTitle(), "")) ? "N/A" : e.getStream().getTitle())
                    .replace("%\\{game}", (Objects.equals(e.getStream().getGameName(), "")) ? "N/A" : e.getStream().getGameName())
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
                        (listenable.getIcons().getTitleIcon() +
                                " " +
                                String.join(", ", currentMessage) +
                                "@" +
                                name +
                                ", " +
                                " " +
                                listenable.getIcons().getTitleIcon()).length() > 500
                ) {
                    currentMessage.add("@" + name);
                } else {
                    messages.add(listenable.getIcons().getTitleIcon() + " " + String.join(", ", currentMessage) + " " + listenable.getIcons().getTitleIcon());
                    currentMessage = new ArrayList<>();
                    currentMessage.add("@" + name);
                }
            }

            // Sending messages:
            for (int i = 0; i < messages.size(); i++) {
                if (i == 0) {
                    try {
                        bot.getClient().getHelix().sendChatAnnouncement(
                                bot.getCredential().getAccessToken(),
                                listenable.getChannel().getAliasId().toString(),
                                bot.getCredential().getUserId(),
                                messages.get(i),
                                AnnouncementColor.PRIMARY
                        );
                    } catch (Exception ex) {
                        LOG.error("Couldn't send an announcement message: " + ex);

                        bot.getClient().getChat().sendMessage(
                                channel.getAliasName(),
                                messages.get(i)
                        );
                    }
                } else {
                    bot.getClient().getChat().sendMessage(
                            channel.getAliasName(),
                            messages.get(i)
                    );
                }
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

            messages.add(listenable.getMessages().getCategoryMessage()
                    .replace("%\\{name}", e.getChannel().getName())
                    .replace("%\\{title}", (Objects.equals(e.getStream().getTitle(), "")) ? "N/A" : e.getStream().getTitle())
                    .replace("%\\{game}", (Objects.equals(e.getStream().getGameName(), "")) ? "N/A" : e.getStream().getGameName())
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
                        (listenable.getIcons().getCategoryIcon() +
                                " " +
                                String.join(", ", currentMessage) +
                                "@" +
                                name +
                                ", " +
                                " " +
                                listenable.getIcons().getCategoryIcon()).length() > 500
                ) {
                    currentMessage.add("@" + name);
                } else {
                    messages.add(listenable.getIcons().getCategoryIcon() + " " + String.join(", ", currentMessage) + " " + listenable.getIcons().getCategoryIcon());
                    currentMessage = new ArrayList<>();
                    currentMessage.add("@" + name);
                }
            }

            // Sending messages:
            for (int i = 0; i < messages.size(); i++) {
                if (i == 0) {
                    try {
                        bot.getClient().getHelix().sendChatAnnouncement(
                                bot.getCredential().getAccessToken(),
                                listenable.getChannel().getAliasId().toString(),
                                bot.getCredential().getUserId(),
                                messages.get(i),
                                AnnouncementColor.PRIMARY
                        );
                    } catch (Exception ex) {
                        LOG.error("Couldn't send an announcement message: " + ex);

                        bot.getClient().getChat().sendMessage(
                                channel.getAliasName(),
                                messages.get(i)
                        );
                    }
                } else {
                    bot.getClient().getChat().sendMessage(
                            channel.getAliasName(),
                            messages.get(i)
                    );
                }
            }
        }

        session.close();
    }
}
