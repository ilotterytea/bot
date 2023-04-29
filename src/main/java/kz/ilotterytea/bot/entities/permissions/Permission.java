package kz.ilotterytea.bot.entities.permissions;

/**
 * @author ilotterytea
 * @version 1.4
 */
public enum Permission {
    SUSPENDED(0),
    USER(1),
    VIP(2),
    MOD(3),
    BROADCASTER(4),
    SUPERUSER(5);

    private final int value;

    Permission(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
