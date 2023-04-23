package kz.ilotterytea.bot.thirdpartythings.seventv.api.schemas;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author ilotterytea
 * @since 1.0
 */
public class SevenTVUser {
    private String id;
    private String username;
    @SerializedName("display_name")
    private String displayName;
    private List<UserConnection> connections;

    public SevenTVUser() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<UserConnection> getConnections() {
        return connections;
    }

    public void setConnections(List<UserConnection> connections) {
        this.connections = connections;
    }
}
