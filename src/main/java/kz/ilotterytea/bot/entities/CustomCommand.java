package kz.ilotterytea.bot.entities;

import jakarta.persistence.*;
import kz.ilotterytea.bot.entities.channels.Channel;

/**
 * @author ilotterytea
 * @version 1.0
 */
@Entity
@Table(name = "custom_commands")
public class CustomCommand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, unique = true, nullable = false)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "channel_id", updatable = false, nullable = false)
    private Channel channel;

    @Column(updatable = false, nullable = false)
    private String name;

    @Column(nullable = false)
    private String message;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled;

    @Column(name = "is_global", nullable = false)
    private Boolean isGlobal;

    public CustomCommand(String name, String message, Channel channel) {
        this.channel = channel;
        this.name = name;
        this.message = message;
        this.isEnabled = true;
        this.isGlobal = false;
    }

    public CustomCommand() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getEnabled() {
        return isEnabled;
    }

    public void setEnabled(Boolean enabled) {
        isEnabled = enabled;
    }

    public Boolean getGlobal() {
        return isGlobal;
    }

    public void setGlobal(Boolean global) {
        isGlobal = global;
    }
}
