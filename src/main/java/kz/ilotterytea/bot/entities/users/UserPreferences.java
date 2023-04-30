package kz.ilotterytea.bot.entities.users;

import jakarta.persistence.*;
import kz.ilotterytea.bot.SharedConstants;

/**
 * User preferences
 * @author ilotterytea
 * @version 1.4
 */
@Entity
@Table(name = "user_preferences")
public class UserPreferences {
    @Id
    @OneToOne
    @JoinColumn(name = "user_id", updatable = false, nullable = false)
    private User user;

    @Column
    private String language;

    public UserPreferences(User user) {
        this.user = user;
        this.language = SharedConstants.DEFAULT_LOCALE_ID;
    }

    public UserPreferences() {}

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
