package kz.ilotterytea.bot.entities;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Preferences {
    @Id
    @Column(name = "channel_id")
    private Integer id;

    @MapsId
    @OneToOne
    @JoinColumn(name = "channel_id")
    private Channel channel;
    private String prefix;
    private String locale;
    private List<String> features;
    @Column(name = "notify_stv")
    private Boolean notifySTVEvents;

    public Preferences(Integer id) {
        this.id = id;
    }

    public Channel getChannel() {
        return channel;
    }
    public void setChannel(Channel channel) {
        this.channel = channel;
    }
    public Integer getId() {
        return id;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getLocale() {
        return locale;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public Boolean getNotifySTVEvents() {
        return notifySTVEvents;
    }

    public void setNotifySTVEvents(Boolean notifySTVEvents) {
        this.notifySTVEvents = notifySTVEvents;
    }
}
