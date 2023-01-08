package kz.ilotterytea.bot.models;

import java.util.UUID;

/**
 * Custom command.
 * @author ilotterytea
 * @since 1.1
 */
public class CustomCommand {
    /** The UUID of the custom command. */
    private final String uuid;
    /** The call ID of the custom command. */
    private String id;
    /** The response of the custom command. */
    private String response;
    /** Command status: enabled or disabled. */
    private boolean value;

    public CustomCommand(
            String id,
            String response,
            boolean value
    ) {
        this.id = id;
        this.response = response;
        this.value = value;
        this.uuid = UUID.randomUUID().toString();
    }

    public String getUUID() { return uuid; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public boolean getValue() { return value; }
    public void setValue(boolean value) { this.value = value; }
}
