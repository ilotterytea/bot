package kz.ilotterytea.bot.utils;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * @author ilotterytea
 * @version 1.0
 */
public class HibernateUtil {
    private static final SessionFactory sessionFactory = new Configuration()
            .configure()
            .buildSessionFactory();

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
