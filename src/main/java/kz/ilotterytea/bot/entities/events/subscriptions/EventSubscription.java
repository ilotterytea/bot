package kz.ilotterytea.bot.entities.events.subscriptions;

import jakarta.persistence.*;
import kz.ilotterytea.bot.entities.events.Event;
import kz.ilotterytea.bot.entities.users.User;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/**
 * Entity for event subscription.
 * @author ilotterytea
 * @version 1.6
 */
@Entity
@Table(name = "event_subscriptions")
public class EventSubscription {
    @Id
    @UuidGenerator
    @Column(nullable = false, unique = true, updatable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false, updatable = false)
    private Event event;

    public EventSubscription(User user, Event event) {
        this.user = user;
        this.event = event;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}
