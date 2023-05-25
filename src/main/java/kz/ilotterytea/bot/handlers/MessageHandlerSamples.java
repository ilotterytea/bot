package kz.ilotterytea.bot.handlers;

import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.entities.CustomCommand;
import kz.ilotterytea.bot.entities.channels.Channel;
import kz.ilotterytea.bot.entities.channels.ChannelPreferences;
import kz.ilotterytea.bot.entities.permissions.Permission;
import kz.ilotterytea.bot.entities.permissions.UserPermission;
import kz.ilotterytea.bot.entities.users.UserPreferences;
import kz.ilotterytea.bot.utils.HibernateUtil;
import kz.ilotterytea.bot.utils.ParsedMessage;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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
            session.getTransaction().commit();
            session.close();
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
            session.getTransaction().commit();
            session.close();
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
            session.getTransaction().begin();

        	Optional<String> response = bot.getLoader().call(
        			parsedMessage.get().getCommandId(),
                    session,
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

            session.getTransaction().commit();
            session.close();
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
}
