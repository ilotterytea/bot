package kz.ilotterytea.bot.thirdpartythings.seventv.api.schemas;

import com.google.gson.annotations.SerializedName;
import kz.ilotterytea.bot.thirdpartythings.seventv.api.schemas.emoteset.EmoteSet;

/**
 * @author ilotterytea
 * @since 1.0
 */
public class UserConnection {
    private String id;
    private String platform;
    private String username;
    @SerializedName("display_name")
    private String displayName;
    @SerializedName("emote_set_id")
    private String emoteSetId;
    @SerializedName("emote_set")
    private EmoteSet emoteSet;

    public UserConnection() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
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

    public String getEmoteSetId() {
        return emoteSetId;
    }

    public void setEmoteSetId(String emoteSetId) {
        this.emoteSetId = emoteSetId;
    }

    public EmoteSet getEmoteSet() {
        return emoteSet;
    }

    public void setEmoteSet(EmoteSet emoteSet) {
        this.emoteSet = emoteSet;
    }
}
