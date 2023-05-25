package kz.ilotterytea.bot.entities.users;

import jakarta.persistence.*;
import kz.ilotterytea.bot.entities.Action;
import kz.ilotterytea.bot.entities.events.subscriptions.EventSubscription;
import kz.ilotterytea.bot.entities.permissions.Permission;
import kz.ilotterytea.bot.entities.permissions.UserPermission;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * User.
 * @author ilotterytea
 * @version 1.4
 */
@Entity
@Table(name = "users")
public class User {
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

    @OneToOne(mappedBy = "user", cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    private UserPreferences preferences;

    @OneToMany(mappedBy = "user", cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    private Set<EventSubscription> subscriptions;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "global_permission", nullable = false)
    private Permission globalPermission;

    @OneToMany(mappedBy = "user", cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    private Set<UserPermission> permissions;

    @OneToMany(mappedBy = "user", cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    private Set<Action> actions;

    public User(Integer aliasId, String aliasName) {
        this.aliasId = aliasId;
        this.aliasName = aliasName;
        this.subscriptions = new HashSet<>();
        this.permissions = new HashSet<>();
        this.actions = new HashSet<>();
    }

    public User() {}

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

    public UserPreferences getPreferences() {
        return preferences;
    }

    public void setPreferences(UserPreferences preferences) {
        preferences.setUser(this);
        this.preferences = preferences;
    }

    public Set<EventSubscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<EventSubscription> subscriptions) {
        for (EventSubscription subscription : subscriptions) {
            subscription.setUser(this);
        }
        this.subscriptions = subscriptions;
    }

    public void addSubscription(EventSubscription subscription) {
        subscription.setUser(this);
        this.subscriptions.add(subscription);
    }

    public void removeSubscription(EventSubscription subscription) {
        this.subscriptions.remove(subscription);
    }

    public Permission getGlobalPermission() {
        return globalPermission;
    }

    public void setGlobalPermission(Permission globalPermission) {
        this.globalPermission = globalPermission;
    }

    public Set<UserPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<UserPermission> permissions) {
        for (UserPermission permission : permissions) {
            permission.setUser(this);
        }

        this.permissions = permissions;
    }

    public void addPermission(UserPermission permission) {
        permission.setUser(this);
        this.permissions.add(permission);
    }

    public void removePermission(UserPermission permission) {
        this.permissions.remove(permission);
    }

    public Set<Action> getActions() {
        return actions;
    }

    public void setActions(Set<Action> actions) {
        for (Action action : actions) {
            action.setUser(this);
        }
        this.actions = actions;
    }

    public void addAction(Action action) {
        action.setUser(this);
        this.actions.add(action);
    }

    public void removeAction(Action action) {
        this.actions.remove(action);
    }
}
