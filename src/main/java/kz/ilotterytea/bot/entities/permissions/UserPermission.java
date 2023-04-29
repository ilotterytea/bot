package kz.ilotterytea.bot.entities.permissions;

import jakarta.persistence.*;
import kz.ilotterytea.bot.entities.channels.Channel;
import kz.ilotterytea.bot.entities.users.User;

/**
 * @author ilotterytea
 * @version 1.4
 */
@Entity
@Table(name = "user_permissions")
public class UserPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, updatable = false, nullable = false)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "channel_id", updatable = false, nullable = false)
    private Channel channel;

    @ManyToOne
    @JoinColumn(name = "user_id", updatable = false, nullable = false)
    private User user;

    @Column(nullable = false)
    private Permission permission;

    public UserPermission() {}

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }
}
