package kz.ilotterytea.bot.thirdpartythings.seventv.v1.models;

/**
 * Emote event update model.
 * @author ilotterytea
 * @since 1.1
 */
public class EmoteEventUpdate {
    /** The channel this update affects. */
    private final String channel;
    /** The ID of the emote. */
    private final String emote_id;
    /** The name or channel alias of the emote. */
    private final String name;
    /** The action done. */
    private final String action;
    /** The user who caused this event to trigger. */
    private final String actor;
    /** An emote object. Null if the action is "REMOVE". */
    private final ExtraEmoteData emote;

    public EmoteEventUpdate(
            String channel,
            String emote_id,
            String name,
            String action,
            String actor,
            ExtraEmoteData emote
    ) {
        this.channel = channel;
        this.emote_id = emote_id;
        this.name = name;
        this.action = action;
        this.actor = actor;
        this.emote = emote;
    }

    public String getChannel() { return channel; }
    public String getEmoteId() { return emote_id; }
    public String getName() { return name; }
    public String getAction() { return action; }
    public String getActor() { return actor; }
    public ExtraEmoteData getEmote() { return emote; }
}
