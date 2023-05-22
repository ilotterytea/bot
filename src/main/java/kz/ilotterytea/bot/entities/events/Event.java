package kz.ilotterytea.bot.entities.events;

import jakarta.persistence.*;
import kz.ilotterytea.bot.entities.channels.Channel;
import kz.ilotterytea.bot.entities.events.subscriptions.EventSubscription;
import org.hibernate.annotations.UuidGenerator;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entity for events.
 * @author ilotterytea
 * @version 1.6
 */
@Entity
@Table(name = "events")
public class Event {
    @Id
    @UuidGenerator
    @Column(nullable = false, unique = true, updatable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "channel_id", updatable = false, nullable = false)
    private Channel channel;

    @Column(name = "alias_id", updatable = false)
    private Integer aliasId;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "event_type", nullable = false, updatable = false)
    private EventType eventType;

    @Column(name = "event_name", updatable = false)
    private String eventName;

    @Column(name = "event_message", nullable = false)
    private String eventMessage;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private Set<EventFlag> flags;

    @OneToMany(mappedBy = "event", cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    private Set<EventSubscription> subscriptions;

    public Event(Integer aliasId, EventType eventType, String eventName, String eventMessage) {
        this.aliasId = aliasId;
        this.eventType = eventType;
        this.eventName = eventName;
        this.eventMessage = eventMessage;
        this.flags = new HashSet<>();
        this.subscriptions = new HashSet<>();
    }

    @PrePersist
    private void prePersist() {
        if (eventType != EventType.CUSTOM && aliasId == null) {
            throw new IllegalStateException("aliasId is required for non-custom events!");
        }

        if (eventType == EventType.CUSTOM && eventName == null) {
            throw new IllegalStateException("eventName is required for custom events!");
        }
    }

    public UUID getId() {
        return id;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Integer getAliasId() {
        return aliasId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventMessage() {
        return eventMessage;
    }

    public void setEventMessage(String eventMessage) {
        this.eventMessage = eventMessage;
    }

    public Set<EventFlag> getFlags() {
        return flags;
    }

    public void setFlags(Set<EventFlag> flags) {
        this.flags = flags;
    }

    public void addFlag(EventFlag flag) {
        this.flags.add(flag);
    }

    public void removeFlag(EventFlag flag) {
        this.flags.remove(flag);
    }

    public Set<EventSubscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<EventSubscription> subscriptions) {
        for (EventSubscription subscription : subscriptions) {
            subscription.setEvent(this);
        }

        this.subscriptions = subscriptions;
    }

    public void addSubscription(EventSubscription subscription) {
        subscription.setEvent(this);
        this.subscriptions.add(subscription);
    }

    public void removeSubscription(EventSubscription subscription) {
        subscription.setEvent(null);
        this.subscriptions.remove(subscription);
    }
}
