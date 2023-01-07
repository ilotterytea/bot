package kz.ilotterytea.bot.thirdpartythings.seventv.v1.models;

/**
 * The emote uploader data.
 * @author ilotterytea
 * @since 1.1
 */
public class OwnerData {
    /** 7TV ID of the owner. */
    private final String id;
    /** Twitch ID of the owner. */
    private final String twitch_id;
    /** Twitch DisplayName of the owner. */
    private final String display_name;
    /** Twitch Login of the owner. */
    private final String login;

    public OwnerData(
            String id,
            String twitch_id,
            String display_name,
            String login
    ) {
        this.id = id;
        this.twitch_id = twitch_id;
        this.display_name = display_name;
        this.login = login;
    }

    public String getId() { return id; }
    public String getTwitchId() { return twitch_id; }
    public String getDisplayName() { return display_name; }
    public String getLogin() { return login; }
}
