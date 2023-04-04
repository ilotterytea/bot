package kz.ilotterytea.bot.thirdpartythings.seventv.v3.schemas.wss;

public class PushedObject<T> {
    private String key;
    private Integer index;
    private String type;
    private T value;

    public PushedObject() {}

    public String getKey() {
        return key;
    }

    public Integer getIndex() {
        return index;
    }

    public String getType() {
        return type;
    }

    public T getValue() {
        return value;
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

    public void setValue(T value) {
        this.value = value;
    }
}
