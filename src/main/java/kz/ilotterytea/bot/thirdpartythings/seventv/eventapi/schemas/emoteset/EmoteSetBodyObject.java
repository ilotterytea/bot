package kz.ilotterytea.bot.thirdpartythings.seventv.eventapi.schemas.emoteset;

import com.google.gson.annotations.SerializedName;

/**
 * @author ilotterytea
 * @since 1.0
 */
public class EmoteSetBodyObject {
    private String key;
    private Integer index;
    private String type;
    private Emote value;
    @SerializedName("old_value")
    private Emote oldValue;

    public EmoteSetBodyObject() {}

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Emote getValue() {
        return value;
    }

    public void setValue(Emote value) {
        this.value = value;
    }

    public Emote getOldValue() {
        return oldValue;
    }

    public void setOldValue(Emote oldValue) {
        this.oldValue = oldValue;
    }
}
