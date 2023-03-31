package kz.ilotterytea.bot.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Table(name = "channels")
public class Channel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @Column(name = "alias_id", updatable = false, nullable = false, unique = true)
    private Integer aliasId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @OneToOne(mappedBy = "channel", cascade = CascadeType.MERGE, fetch = FetchType.LAZY, optional = false)
    @PrimaryKeyJoinColumn
    private Preferences preferences;

    public Channel(Integer aliasId) {
        this.aliasId = aliasId;
    }

    public Integer getId() {
        return id;
    }

    public Integer getAliasId() {
        return aliasId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setPreferences(Preferences preferences) {
        this.preferences = preferences;
        this.preferences.setChannel(this);
    }
}
