package kz.ilotterytea.bot.thirdpartythings.seventv.eventapi.schemas;

/**
 * @author ilotterytea
 * @since 1.0
 */
public class PayloadData<T> {
    private String type;
    private ConditionData condition;
    private T body;

    public PayloadData(String type, ConditionData condition) {
        this.type = type;
        this.condition = condition;
    }

    public PayloadData(String type, T body) {
        this.type = type;
        this.body = body;
    }

    public PayloadData() {}

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ConditionData getCondition() {
        return condition;
    }

    public void setCondition(ConditionData condition) {
        this.condition = condition;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }
}