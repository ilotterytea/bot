package kz.ilotterytea.bot.thirdpartythings.seventv.v1.models;

/**
 * The owner data with role.
 * @author ilotterytea
 * @since 1.1
 */
public class OwnerDataWithRole extends OwnerData {
    /** The owner's role. */
    private final RoleData role;

    public OwnerDataWithRole(
            String id,
            String twitch_id,
            String login,
            String display_name,
            RoleData role

    ) {
        super(id, twitch_id, display_name, login);
        this.role = role;
    }

    public RoleData getRole() { return role; }
}
