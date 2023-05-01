package kz.ilotterytea.bot.entities.subscribers;

import com.google.common.collect.Sets;
import jakarta.persistence.*;
import kz.ilotterytea.bot.entities.listenables.Listenable;
import kz.ilotterytea.bot.entities.users.User;

import java.util.Set;

/**
 * Subscriber for listenable.
 * @author ilotterytea
 * @version 1.4
 */
@Entity
@Table(name = "subscribers")
public class Subscriber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, updatable = false, nullable = false)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", updatable = false, nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "listenable_id", updatable = false, nullable = false)
    private Listenable listenable;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private Set<SubscriberEvent> events;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled;

    public Subscriber(User user, Listenable listenable) {
        this.user = user;
        this.listenable = listenable;
        this.events = Sets.newHashSet(SubscriberEvent.LIVE, SubscriberEvent.OFFLINE, SubscriberEvent.TITLE, SubscriberEvent.CATEGORY);
        this.isEnabled = true;
    }

    public Subscriber() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Listenable getListenable() {
        return listenable;
    }

    public void setListenable(Listenable listenable) {
        this.listenable = listenable;
    }

    public Set<SubscriberEvent> getEvents() {
        return events;
    }

    public void setEvents(Set<SubscriberEvent> events) {
        this.events = events;
    }

    public Boolean getEnabled() {
        return isEnabled;
    }

    public void setEnabled(Boolean enabled) {
        isEnabled = enabled;
    }
}
