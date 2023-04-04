package kz.ilotterytea.bot.thirdpartythings.seventv.v3.schemas.wss;

public class PulledObject<T> {
    private String key;
    private Integer index;
    private String type;
    private T old_value;

    public PulledObject() {}

    public String getKey() {
        return key;
    }

    public Integer getIndex() {
        return index;
    }

    public String getType() {
        return type;
    }

    public T getOldValue() {
        return old_value;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setOldValue(T old_value) {
        this.old_value = old_value;
    }
}
