package kz.ilotterytea.bot.utils;

import kz.ilotterytea.bot.entities.Action;
import kz.ilotterytea.bot.entities.CustomCommand;
import kz.ilotterytea.bot.entities.Timer;
import kz.ilotterytea.bot.entities.channels.Channel;
import kz.ilotterytea.bot.entities.channels.ChannelPreferences;
import kz.ilotterytea.bot.entities.events.Event;
import kz.ilotterytea.bot.entities.events.subscriptions.EventSubscription;
import kz.ilotterytea.bot.entities.permissions.UserPermission;
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
            .addAnnotatedClass(Event.class)
            .addAnnotatedClass(EventSubscription.class)
            .addAnnotatedClass(CustomCommand.class)
            .addAnnotatedClass(UserPermission.class)
            .addAnnotatedClass(Timer.class)
            .addAnnotatedClass(Action.class)
            .buildSessionFactory();

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
