package kz.ilotterytea.bot.entities;

import jakarta.persistence.*;
import kz.ilotterytea.bot.entities.channels.Channel;
import kz.ilotterytea.bot.entities.users.User;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

/**
 * Action entity.
 * @author ilotterytea
 * @version 1.5
 */
@Entity
@Table(name = "actions")
public class Action {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true, updatable = false)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "channel_id", nullable = false, updatable = false)
    private Channel channel;

    @Column(name = "command_id", nullable = false, updatable = false)
    private String commandId;

    @Column(name = "full_message", nullable = false, updatable = false)
    private String fullMessage;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "executed_at", nullable = false, updatable = false)
    private Date creationTimestamp;

    public Action(User user, Channel channel, String commandId, String fullMessage) {
        this.user = user;
        this.channel = channel;
        this.commandId = commandId;
        this.fullMessage = fullMessage;
    }

    public Action() {}

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

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getCommandId() {
        return commandId;
    }

    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }

    public String getFullMessage() {
        return fullMessage;
    }

    public void setFullMessage(String fullMessage) {
        this.fullMessage = fullMessage;
    }

    public Date getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(Date creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }
}
