package kz.ilotterytea.bot.thirdpartythings.seventv.eventapi.schemas;

/**
 * @author ilotterytea
 * @since 1.0
 */
public class AcknowledgeData<T> {
    private String command;
    private T data;

    public AcknowledgeData() {}

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
