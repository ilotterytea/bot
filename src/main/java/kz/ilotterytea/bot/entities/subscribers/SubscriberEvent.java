package kz.ilotterytea.bot.entities.subscribers;

/**
 * @author ilotterytea
 * @version 1.0
 */
public enum SubscriberEvent {
    LIVE("LIVE"),
    OFFLINE("OFFLINE"),
    TITLE("TITLE"),
    CATEGORY("CATEGORY");

    private String name;

    SubscriberEvent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
