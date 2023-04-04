package kz.ilotterytea.bot.thirdpartythings.seventv.v3.schemas.wss;

import java.util.Map;

public class MessageData<T> {
    private String type;
    private Map<String, String> condition;
    private T body;

    public MessageData(String type, Map<String, String> condition, T body) {
        this.type = type;
        this.condition = condition;
        this.body = body;
    }

    public MessageData() {}

    public String getType() {
        return type;
    }

    public Map<String, String> getCondition() {
        return condition;
    }

    public T getBody() {
        return body;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCondition(Map<String, String> condition) {
        this.condition = condition;
    }

    public void setBody(T body) {
        this.body = body;
    }
}
