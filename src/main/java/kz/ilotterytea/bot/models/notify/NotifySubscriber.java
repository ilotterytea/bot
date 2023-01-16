package kz.ilotterytea.bot.models.notify;

import java.util.ArrayList;

/**
 * Notification subscriber.
 * @author ilotterytea
 * @since 1.3
 */
public class NotifySubscriber {
    /** Subscribed events. */
    private final ArrayList<String> subscribed;
    /** Alias ID. */
    private final String aliasId;

    public NotifySubscriber(
            ArrayList<String> subscribed,
            String aliasId
    ) {
        this.aliasId = aliasId;
        this.subscribed = subscribed;
    }

    public String getAliasId() { return aliasId; }
    public ArrayList<String> getSubscribedEvents() { return subscribed; }
    public void subscribeToEvent(String event) {
        if (this.subscribed.contains(event.toLowerCase())) {
            return;
        }

        this.subscribed.add(event.toLowerCase());
    }

    public void unsubscribeFromEvent(String event) {
        this.subscribed.remove(event);
    }
}
