package kz.ilotterytea.bot.builtin;

import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.api.permissions.Permissions;
import kz.ilotterytea.bot.entities.channels.Channel;
import kz.ilotterytea.bot.entities.channels.ChannelPreferences;
import kz.ilotterytea.bot.entities.permissions.UserPermission;
import kz.ilotterytea.bot.entities.users.User;
import kz.ilotterytea.bot.i18n.LineIds;
import kz.ilotterytea.bot.models.ArgumentsModel;
import kz.ilotterytea.bot.utils.HibernateUtil;
import org.hibernate.Session;

import java.util.*;

/**
 * Ping command.
 * @author ilotterytea
 * @since 1.3
 */
public class SetterCommand extends Command {
    @Override
    public String getNameId() { return "set"; }

    @Override
    public int getDelay() { return 5000; }

    @Override
    public Permissions getPermissions() { return Permissions.BROADCASTER; }

    @Override
    public ArrayList<String> getOptions() { return new ArrayList<>(Collections.singletonList("self")); }

    @Override
    public ArrayList<String> getSubcommands() { return new ArrayList<>(Arrays.asList("prefix", "locale", "notify-7tv")); }

    @Override
    public ArrayList<String> getAliases() { return new ArrayList<>(); }

    @Override
    public String run(ArgumentsModel m) {
        if (m.getMessage().getSubCommand() == null) {
            return Huinyabot.getInstance().getLocale().literalText(
                    m.getLanguage(),
                    LineIds.NO_SUBCMD
            );
        }

        List<String> s = List.of(m.getMessage().getMessage().split(" "));

        Session session = HibernateUtil.getSessionFactory().openSession();

        // Getting the origin channel's info:
        List<Channel> channels = session.createQuery("from Channel where aliasId = :aliasId AND optOutTimestamp is null", Channel.class)
                .setParameter("aliasId", m.getEvent().getChannel().getId())
                .getResultList();

        Channel channel = channels.get(0);

        // Getting the sender's info:
        List<User> users = session.createQuery("from User where aliasId = :aliasId AND optOutTimestamp is null", User.class)
                .setParameter("aliasId", m.getEvent().getUser().getId())
                .getResultList();

        User user = users.get(0);

        // Getting the sender's permission:
        List<UserPermission> permissions = session.createQuery("from UserPermission where user = :user AND channel = :channel", UserPermission.class)
                .setParameter("user", user)
                .setParameter("channel", channel)
                .getResultList();

        UserPermission permission = permissions.get(0);

        // Broadcaster:
        if (permission.getPermission().getValue() >= Permissions.BROADCASTER.getId()) {
            switch (m.getMessage().getSubCommand()) {
                // "Prefix" clause.
                case "prefix": {
                    if (s.isEmpty()) {
                        return Huinyabot.getInstance().getLocale().literalText(
                                channel.getPreferences().getLanguage(),
                                LineIds.NOT_ENOUGH_ARGS
                        );
                    }

                    ChannelPreferences preferences = channel.getPreferences();
                    preferences.setPrefix(m.getMessage().getMessage().toLowerCase());
                    channel.setPreferences(preferences);

                    session.getTransaction().begin();

                    session.persist(user);
                    session.persist(preferences);

                    session.getTransaction().commit();

                    return Huinyabot.getInstance().getLocale().formattedText(
                            preferences.getLanguage(),
                            LineIds.C_SET_SUCCESS_PREFIX_SET,
                            preferences.getPrefix()
                    );
                }
                // "Locale", "language" clause.
                case "locale":
                    if (
                            s.isEmpty() ||
                            !Huinyabot.getInstance().getLocale().getLocaleIds().contains(m.getMessage().getMessage().toLowerCase())
                    ) {
                        return Huinyabot.getInstance().getLocale().formattedText(
                                channel.getPreferences().getLanguage(),
                                LineIds.C_SET_SUCCESS_LOCALE_LIST,
                                String.join(", ", Huinyabot.getInstance().getLocale().getLocaleIds())
                        );
                    }

                    ChannelPreferences preferences = channel.getPreferences();
                    preferences.setLanguage(m.getMessage().getMessage().toLowerCase());
                    channel.setPreferences(preferences);

                    session.getTransaction().begin();

                    session.persist(user);
                    session.persist(preferences);

                    session.getTransaction().commit();

                    return Huinyabot.getInstance().getLocale().literalText(
                            preferences.getLanguage(),
                            LineIds.C_SET_SUCCESS_LOCALE_SET
                    );
            }
        }

        return Huinyabot.getInstance().getLocale().literalText(
                channel.getPreferences().getLanguage(),
                LineIds.NO_RIGHTS
        );
    }
}
