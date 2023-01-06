package kz.ilotterytea.bot.api.permissions;

/**
 * User permissions.
 * @author ilotterytea
 * @since 1.0
 */
public enum Permissions {
    SUSPENDED(0),
    USER(1),
    VIP(2),
    MOD(3),
    TRUSTED(4),
    BROADCASTER(5),
    SUPAUSER(127),
    ;

    private final int id;
    Permissions(int id) {
        this.id = id;
    }
    public int getId() { return id; }
}
