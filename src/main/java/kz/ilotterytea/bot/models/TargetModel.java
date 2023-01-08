package kz.ilotterytea.bot.models;

import kz.ilotterytea.bot.models.emotes.Emote;
import kz.ilotterytea.bot.models.emotes.Provider;

import java.util.Map;

/**
 * Target (channel) model.
 * @author ilotterytea
 * @since 1.0
 */
public class TargetModel {
    /** Target's alias ID.
     * This is the channel ID from the platform the bot took it from.
     * In the case of Twitch, it could be the user ID.
     */
    private final String aliasId;
    /** Target's chat statistics. */
    private final StatsModel stats;
    /** Target's emotes. */
    private Map<Provider, Map<String, Emote>> emotes;
    /** Target's custom commands. */
    private Map<String, CustomCommand> custom;

    public TargetModel(
            String aliasId,
            StatsModel stats,
            Map<Provider, Map<String, Emote>> emotes,
            Map<String, CustomCommand> customCommands
    ) {
        this.aliasId = aliasId;
        this.stats = stats;
        this.emotes = emotes;
        this.custom = customCommands;
    }

    public StatsModel getStats() { return stats; }
    public String getAliasId() { return aliasId; }
    public Map<Provider, Map<String, Emote>> getEmotes() { return emotes; }
    public void setEmotes(Provider provider, Map<String, Emote> emotes) { this.emotes.put(provider, emotes); }
    public Map<String, CustomCommand> getCustomCommands() { return custom; }
    public void setCustomCommands(Map<String, CustomCommand> commands) { this.custom = commands; }
}
