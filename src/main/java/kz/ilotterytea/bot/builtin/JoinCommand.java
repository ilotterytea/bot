package kz.ilotterytea.bot.builtin;

import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.api.permissions.Permissions;
import kz.ilotterytea.bot.entities.channels.Channel;
import kz.ilotterytea.bot.entities.channels.ChannelPreferences;
import kz.ilotterytea.bot.entities.users.User;
import kz.ilotterytea.bot.entities.users.UserPreferences;
import kz.ilotterytea.bot.i18n.LineIds;
import kz.ilotterytea.bot.models.ArgumentsModel;
import kz.ilotterytea.bot.utils.HibernateUtil;
import org.hibernate.Session;

import java.util.*;

/**
 * Join command.
 * @author ilotterytea
 * @since 1.1
 */
public class JoinCommand extends Command {
    @Override
    public String getNameId() { return "join"; }

    @Override
    public int getDelay() { return 120000; }

    @Override
    public Permissions getPermissions() { return Permissions.USER; }

    @Override
    public ArrayList<String> getOptions() { return new ArrayList<>(Arrays.asList("silent", "тихо", "only-listen")); }

    @Override
    public ArrayList<String> getSubcommands() { return new ArrayList<>(); }

    @Override
    public ArrayList<String> getAliases() { return new ArrayList<>(Arrays.asList("зайти")); }

    @Override
    public String run(ArgumentsModel m) {
        Session session = HibernateUtil.getSessionFactory().openSession();

        // Getting the sender's local user info:
        List<User> users = session.createQuery("from User where aliasId = :aliasId", User.class)
                .setParameter("aliasId", m.getEvent().getUser().getId())
                .getResultList();

        User user;

        session.getTransaction().begin();

        if (users.isEmpty()) {
            user = new User(Integer.parseInt(m.getEvent().getUser().getId()), m.getEvent().getUser().getName());
            UserPreferences preferences = new UserPreferences(user);
            user.setPreferences(preferences);

            session.persist(user);
            session.persist(preferences);

            session.getTransaction().commit();
        } else {
            user = users.get(0);
        }

        // Getting the channel's local info if it exists:
        List<Channel> channels = session.createQuery("from Channel where aliasId = :aliasId", Channel.class)
                .setParameter("aliasId", m.getEvent().getChannel().getId())
                .getResultList();

        Channel originChannel = channels.get(0);

        // Getting the sender's local channel info if it exists:
        channels = session.createQuery("from Channel where aliasId = :aliasId", Channel.class)
                .setParameter("aliasId", user.getAliasId())
                .getResultList();

        Channel channel;

        // Creating a new channel if it does not exist:
        if (channels.isEmpty()) {
            channel = new Channel(user.getAliasId(), user.getAliasName());
            ChannelPreferences preferences = new ChannelPreferences(channel);
            channel.setPreferences(preferences);

            session.persist(channel);
            session.persist(preferences);

            session.getTransaction().commit();
        } else {
            channel = channels.get(0);

            // If the channel has already been opt-outed, opt-in:
            if (channel.getOptOutTimestamp() != null) {
                channel.setOptOutTimestamp(null);
            } else {
                session.close();
                return Huinyabot.getInstance().getLocale().formattedText(
                        originChannel.getPreferences().getLanguage(),
                        LineIds.C_JOIN_ALREADYIN,
                        channel.getAliasName()
                );
            }
        }

        session.close();

        Huinyabot.getInstance().getClient().getChat().joinChannel(m.getEvent().getUser().getName());

        return Huinyabot.getInstance().getLocale().formattedText(
                originChannel.getPreferences().getLanguage(),
                LineIds.C_JOIN_SUCCESS,
                channel.getAliasName()
        );
    }
}
