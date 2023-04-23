package kz.ilotterytea.bot.thirdpartythings.seventv.api.schemas;

import com.google.gson.annotations.SerializedName;
import kz.ilotterytea.bot.thirdpartythings.seventv.api.schemas.emoteset.EmoteSet;

/**
 * @author ilotterytea
 * @since 1.0
 */
public class User {
    private String id;
    private String platform;
    @SerializedName("display_name")
    private String displayName;
    @SerializedName("linked_at")
    private Long linkedAt;
    @SerializedName("emote_set_id")
    private String emoteSetId;
    @SerializedName("emote_set")
    private EmoteSet emoteSet;
    private SevenTVUser user;

    public User() {}

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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Long getLinkedAt() {
        return linkedAt;
    }

    public void setLinkedAt(Long linkedAt) {
        this.linkedAt = linkedAt;
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

    public SevenTVUser getUser() {
        return user;
    }

    public void setUser(SevenTVUser user) {
        this.user = user;
    }
}
