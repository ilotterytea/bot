package kz.ilotterytea.bot.models.notify;

import java.util.ArrayList;
import java.util.Map;

/**
 * A listener for notification.
 * @author ilotterytea
 * @since 1.3
 */
public class NotifyListener {
    /** Messages to replace the standard ones. */
    private final Map<String, String> messages;
    /** Icons. */
    private final Map<String, String> icons;
    /** Subscribed events. */
    private final ArrayList<String> events;
    /** Subscribers. */
    private final ArrayList<NotifySubscriber> subscribers;
    /** Flags. */
    private final Map<String, ArrayList<String>> flags;

    public NotifyListener(
            Map<String, String> messages,
            Map<String, String> icons,
            ArrayList<String> events,
            ArrayList<NotifySubscriber> subscribers,
            Map<String, ArrayList<String>> flags
    ) {
        this.messages = messages;
        this.icons = icons;
        this.events = events;
        this.subscribers = subscribers;
        this.flags = flags;
    }

    public Map<String, String> getMessages() { return messages; }
    public Map<String, String> getIcons() { return icons; }
    public ArrayList<String> getEvents() { return events; }
    public Map<String, ArrayList<String>> getFlags() { return flags; }
    public ArrayList<NotifySubscriber> getSubscribers() { return subscribers; }
}
