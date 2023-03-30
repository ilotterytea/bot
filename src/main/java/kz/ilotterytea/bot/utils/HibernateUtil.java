package kz.ilotterytea.bot.utils;

import jakarta.persistence.EntityManager;
import kz.ilotterytea.bot.entities.channels.Channel;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            Configuration configuration = new Configuration();
            SessionFactory factory = configuration.configure().addAnnotatedClass(Channel.class).buildSessionFactory();
            return factory;
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}

