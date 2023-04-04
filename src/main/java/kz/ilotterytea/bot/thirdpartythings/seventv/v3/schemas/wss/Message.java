package kz.ilotterytea.bot.thirdpartythings.seventv.v3.schemas.wss;

public class Message<T> {
    private Integer op;
    private T d;

    public Message(Integer op, T d) {
        this.op = op;
        this.d = d;
    }

    public Integer getOp() {
        return op;
    }

    public T getD() {
        return d;
    }

    public void setOp(Integer op) {
        this.op = op;
    }

    public void setD(T d) {
        this.d = d;
    }
}
