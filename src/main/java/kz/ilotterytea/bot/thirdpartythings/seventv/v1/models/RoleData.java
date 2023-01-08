package kz.ilotterytea.bot.thirdpartythings.seventv.v1.models;

/**
 * Role data.
 * @author ilotterytea
 * @since 1.1
 */
public class RoleData {
    private final String id;
    private final String name;
    private final int position;
    private final int color;
    private final int allowed;
    private final int denied;

    public RoleData(
            String id,
            String name,
            int position,
            int color,
            int allowed,
            int denied
    ) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.color = color;
        this.allowed = allowed;
        this.denied = denied;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getPosition() { return position; }
    public int getColor() { return color; }
    public int getAllowed() { return allowed; }
    public int getDenied() { return denied; }
}
