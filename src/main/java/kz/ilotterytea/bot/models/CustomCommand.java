package kz.ilotterytea.bot.models;

import java.util.ArrayList;
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
    /** The flags of the custom command. */
    private final ArrayList<String> flags;

    public CustomCommand(
            String id,
            String response,
            boolean value,
            ArrayList<String> flags
    ) {
        this.id = id;
        this.response = response;
        this.value = value;
        this.uuid = UUID.randomUUID().toString();
        this.flags = flags;
    }

    public String getUUID() { return uuid; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public boolean getValue() { return value; }
    public void setValue(boolean value) { this.value = value; }

    public ArrayList<String> getFlags() { return flags; }
    public boolean getFlag(String id) {
        return flags.contains(id);
    }
    public void setFlag(String id) {
        if (!flags.contains(id)) {
            flags.add(id);
        }
    }
    public void removeFlag(String id) {
        flags.remove(id);
    }
}
