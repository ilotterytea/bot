package kz.ilotterytea.bot.entities.listenables;

import jakarta.persistence.*;
import kz.ilotterytea.bot.entities.channels.Channel;

import java.util.HashSet;
import java.util.Set;

/**
 * @author ilotterytea
 * @version 1.0
 */
public class Listenable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "alias_id", updatable = false, nullable = false)
    private Integer aliasId;

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

    public Listenable(Integer aliasId, ListenableMessages messages, ListenableIcons icons) {
        this.aliasId = aliasId;
        this.messages = messages;
        this.icons = icons;
        this.flags = new HashSet<>();
        this.isEnabled = true;
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
        this.messages = messages;
    }

    public ListenableIcons getIcons() {
        return icons;
    }

    public void setIcons(ListenableIcons icons) {
        this.icons = icons;
    }

    public Boolean getEnabled() {
        return isEnabled;
    }

    public void setEnabled(Boolean enabled) {
        isEnabled = enabled;
    }
}
