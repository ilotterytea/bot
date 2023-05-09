package kz.ilotterytea.bot.models.serverresponse.mc;

import java.util.List;
import java.util.Map;

/**
 * Server info model.
 * @author ilotterytea
 * @since 1.5
 */
public class ServerInfo {
    private Boolean online;
    private String hostname;
    private Map<String, Integer> players;
    private String version;
    private Map<String, List<String>> motd;

    public ServerInfo() {}

    public Boolean getOnline() {
        return online;
    }

    public String getHostname() {
        return hostname;
    }

    public Map<String, Integer> getPlayers() {
        return players;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, List<String>> getMotd() {
        return motd;
    }
}
