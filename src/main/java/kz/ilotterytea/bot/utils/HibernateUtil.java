package kz.ilotterytea.bot.utils;

import kz.ilotterytea.bot.entities.channels.Channel;
import kz.ilotterytea.bot.entities.channels.ChannelPreferences;
import kz.ilotterytea.bot.entities.users.User;
import kz.ilotterytea.bot.entities.users.UserPreferences;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * @author ilotterytea
 * @version 1.0
 */
public class HibernateUtil {
    private static final SessionFactory sessionFactory = new Configuration()
            .configure()
            .addAnnotatedClass(Channel.class)
            .addAnnotatedClass(ChannelPreferences.class)
            .addAnnotatedClass(User.class)
            .addAnnotatedClass(UserPreferences.class)
            .buildSessionFactory();

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
