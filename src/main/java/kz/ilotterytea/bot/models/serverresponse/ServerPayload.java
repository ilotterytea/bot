package kz.ilotterytea.bot.models.serverresponse;

/**
 * @author ilotterytea
 * @since 1.0
 */
public class ServerPayload<T> {
    private Integer status;
    private String message;
    private T data;

    public ServerPayload() {}

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
