package kz.ilotterytea.bot.entities;

import jakarta.persistence.*;
import kz.ilotterytea.bot.entities.channels.Channel;

import java.util.Date;

/**
 * Timer entity.
 * @author ilotterytea
 * @since 1.5
 */
@Entity
@Table(name = "timers")
public class Timer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "channel_id", nullable = false, updatable = false)
    private Channel channel;

    @Column(nullable = false, updatable = false)
    private String name;

    @Column(nullable = false)
    private String message;

    @Column(name = "interval_ms", nullable = false)
    private Integer intervalMilliseconds;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_executed_at", nullable = false)
    private Date lastTimeExecuted;

    public Timer(Channel channel, String name, String message, Integer intervalMilliseconds) {
        this.channel = channel;
        this.name = name;
        this.message = message;
        this.intervalMilliseconds = intervalMilliseconds;
        this.lastTimeExecuted = new Date();
    }

    public Timer() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getIntervalMilliseconds() {
        return intervalMilliseconds;
    }

    public void setIntervalMilliseconds(Integer intervalMilliseconds) {
        this.intervalMilliseconds = intervalMilliseconds;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Date getLastTimeExecuted() {
        return lastTimeExecuted;
    }

    public void setLastTimeExecuted(Date lastTimeExecuted) {
        this.lastTimeExecuted = lastTimeExecuted;
    }
}
