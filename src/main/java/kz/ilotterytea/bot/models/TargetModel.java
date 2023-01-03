package kz.ilotterytea.bot.models;

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

    public TargetModel(
            String aliasId,
            StatsModel stats
    ) {
        this.aliasId = aliasId;
        this.stats = stats;
    }

    public StatsModel getStats() { return stats; }
    public String getAliasId() { return aliasId; }
}
