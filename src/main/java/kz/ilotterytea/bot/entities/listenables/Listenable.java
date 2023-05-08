package kz.ilotterytea.bot.entities.listenables;

import jakarta.persistence.*;
import kz.ilotterytea.bot.entities.channels.Channel;
import kz.ilotterytea.bot.entities.subscribers.Subscriber;

import java.util.HashSet;
import java.util.Set;

/**
 * Listenable.
 * @author ilotterytea
 * @version 1.4
 */
@Entity
@Table(name = "listenables")
public class Listenable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "alias_id", updatable = false, nullable = false)
    private Integer aliasId;

    @Column(name = "alias_name", nullable = false)
    private String aliasName;

    @ManyToOne
    @JoinColumn(name = "channel_id", updatable = false, nullable = false)
    private Channel channel;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private Set<ListenableFlag> flags;

    @OneToOne(mappedBy = "listenable", cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    private ListenableMessages messages;

    @OneToOne(mappedBy = "listenable", cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    private ListenableIcons icons;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled;

    @OneToMany(mappedBy = "listenable", cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    private Set<Subscriber> subscribers;

    public Listenable(Integer aliasId, String aliasName, ListenableMessages messages, ListenableIcons icons) {
        this.aliasId = aliasId;
        this.aliasName = aliasName;
        this.messages = messages;
        this.icons = icons;
        this.flags = new HashSet<>();
        this.isEnabled = true;
        this.subscribers = new HashSet<>();
    }

    public Listenable() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAliasId() {
        return aliasId;
    }

    public void setAliasId(Integer aliasId) {
        this.aliasId = aliasId;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Set<ListenableFlag> getFlags() {
        return flags;
    }

    public void setFlags(Set<ListenableFlag> flags) {
        this.flags = flags;
    }

    public void addFlag(ListenableFlag flag) {
        this.flags.add(flag);
    }

    public void removeFlag(ListenableFlag flag) {
        this.flags.remove(flag);
    }

    public ListenableMessages getMessages() {
        return messages;
    }

    public void setMessages(ListenableMessages messages) {
        messages.setListenable(this);
        this.messages = messages;
    }

    public ListenableIcons getIcons() {
        return icons;
    }

    public void setIcons(ListenableIcons icons) {
        icons.setListenable(this);
        this.icons = icons;
    }

    public Boolean getEnabled() {
        return isEnabled;
    }

    public void setEnabled(Boolean enabled) {
        isEnabled = enabled;
    }

    public Set<Subscriber> getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(Set<Subscriber> subscribers) {
        for (Subscriber subscriber : subscribers) {
            subscriber.setListenable(this);
        }
        this.subscribers = subscribers;
    }

    public void addSubscriber(Subscriber subscriber) {
        subscriber.setListenable(this);
        this.subscribers.add(subscriber);
    }

    public void removeSubscriber(Subscriber subscriber) {
        this.subscribers.remove(subscriber);
    }
}
