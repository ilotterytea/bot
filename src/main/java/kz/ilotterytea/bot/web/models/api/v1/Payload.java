package kz.ilotterytea.bot.web.models.api.v1;

/**
 * @author ilotterytea
 * @since 1.4
 */
public class Payload<T> {
    private final int status;
    private final String message;
    private final T data;

    public Payload(
            int status,
            String message,
            T data
    ) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
