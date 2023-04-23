package kz.ilotterytea.bot.thirdpartythings.seventv.eventapi.schemas;

/**
 * @author ilotterytea
 * @since 1.0
 */
public class Payload<T> {
    private Integer op;
    private T d;

    public Payload(Integer op, T d) {
        this.op = op;
        this.d = d;
    }

    public Payload() {}

    public Integer getOperation() {
        return op;
    }

    public void setOperation(Integer op) {
        this.op = op;
    }

    public T getData() {
        return d;
    }

    public void setData(T d) {
        this.d = d;
    }
}
