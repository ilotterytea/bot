package kz.ilotterytea.bot.entities.subscribers;

/**
 * Subscriber's subscribed event.
 * @author ilotterytea
 * @version 1.4
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
