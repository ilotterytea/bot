package kz.ilotterytea.bot.thirdpartythings.seventv.eventapi.schemas.emoteset;

import com.google.gson.annotations.SerializedName;

/**
 * @author ilotterytea
 * @since 1.0
 */
public class EmoteSetUser {
    private String id;
    private String username;
    @SerializedName("display_name")
    private String displayName;

    public EmoteSetUser() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
