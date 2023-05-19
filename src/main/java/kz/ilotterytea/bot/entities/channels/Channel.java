package kz.ilotterytea.bot.entities.channels;

import jakarta.persistence.*;
import kz.ilotterytea.bot.entities.Action;
import kz.ilotterytea.bot.entities.CustomCommand;
import kz.ilotterytea.bot.entities.Timer;
import kz.ilotterytea.bot.entities.listenables.Listenable;
import kz.ilotterytea.bot.entities.permissions.UserPermission;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Channel.
 * @author ilotterytea
 * @version 1.4
 */
@Entity
@Table(name = "channels")
public class Channel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, unique = true, nullable = false)
    private Integer id;

    @Column(name = "alias_id", unique = true, updatable = false, nullable = false)
    private Integer aliasId;

    @Column(name = "alias_name", nullable = false)
    private String aliasName;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", updatable = false, nullable = false)
    private Date creationTimestamp;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = false)
    private Date updateTimestamp;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "opt_outed_at")
    private Date optOutTimestamp;

    @OneToOne(mappedBy = "channel", cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    private ChannelPreferences preferences;

    @OneToMany(mappedBy = "channel", cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    private Set<Listenable> listenables;

    @OneToMany(mappedBy = "channel", cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    private Set<CustomCommand> commands;

    @OneToMany(mappedBy = "channel", cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    private Set<UserPermission> permissions;

    @OneToMany(mappedBy = "channel", cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    private Set<Timer> timers;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private Set<ChannelFeature> features;

    @OneToMany(mappedBy = "channel", cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    private Set<Action> actions;

    public Channel(Integer aliasId, String aliasName) {
        this.aliasId = aliasId;
        this.aliasName = aliasName;
        this.listenables = new HashSet<>();
        this.commands = new HashSet<>();
        this.permissions = new HashSet<>();
        this.features = new HashSet<>();
        this.timers = new HashSet<>();
        this.actions = new HashSet<>();
    }

    public Channel() {}

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

    public Date getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(Date creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public Date getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(Date updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }

    public Date getOptOutTimestamp() {
        return optOutTimestamp;
    }

    public void setOptOutTimestamp(Date optOutTimestamp) {
        this.optOutTimestamp = optOutTimestamp;
    }

    public ChannelPreferences getPreferences() {
        return preferences;
    }

    public void setPreferences(ChannelPreferences preferences) {
        preferences.setChannel(this);
        this.preferences = preferences;
    }

    public Set<Listenable> getListenables() {
        return listenables;
    }

    public void setListenables(Set<Listenable> listenables) {
        for (Listenable listenable : listenables) {
            listenable.setChannel(this);
        }
        this.listenables = listenables;
    }

    public void addListenable(Listenable listenable) {
        listenable.setChannel(this);
        this.listenables.add(listenable);
    }

    public void removeListenable(Listenable listenable) {
        this.listenables.remove(listenable);
    }

    public Set<CustomCommand> getCommands() {
        return commands;
    }

    public void setCommands(Set<CustomCommand> commands) {
        for (CustomCommand command : commands) {
            command.setChannel(this);
        }
        this.commands = commands;
    }

    public void addCommand(CustomCommand command) {
        command.setChannel(this);
        this.commands.add(command);
    }

    public void removeCommand(CustomCommand command) {
        this.commands.remove(command);
    }

    public Set<UserPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<UserPermission> permissions) {
        for (UserPermission permission : permissions) {
            permission.setChannel(this);
        }

        this.permissions = permissions;
    }

    public void addPermission(UserPermission permission) {
        permission.setChannel(this);
        this.permissions.add(permission);
    }

    public void removePermission(UserPermission permission) {
        this.permissions.remove(permission);
    }

    public Set<ChannelFeature> getFeatures() {
        return features;
    }

    public void setFeatures(Set<ChannelFeature> features) {
        this.features = features;
    }

    public void addFeature(ChannelFeature feature) {
        this.features.add(feature);
    }

    public void removeFeature(ChannelFeature feature) {
        this.features.remove(feature);
    }

    public Set<Timer> getTimers() {
        return timers;
    }

    public void setTimers(Set<Timer> timers) {
        for (Timer timer : timers) {
            timer.setChannel(this);
        }

        this.timers = timers;
    }

    public void addTimer(Timer timer) {
        timer.setChannel(this);
        this.timers.add(timer);
    }

    public void removeTimer(Timer timer) {
        timer.setChannel(null);
        this.timers.remove(timer);
    }

    public Set<Action> getActions() {
        return actions;
    }

    public void setActions(Set<Action> actions) {
        for (Action action : actions) {
            action.setChannel(this);
        }
        this.actions = actions;
    }

    public void addAction(Action action) {
        action.setChannel(this);
        this.actions.add(action);
    }

    public void removeAction(Action action) {
        this.actions.remove(action);
    }
}
