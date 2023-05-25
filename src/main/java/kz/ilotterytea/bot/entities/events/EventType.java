package kz.ilotterytea.bot.entities.events;

import java.util.Optional;

/**
 * Types for event.
 * @author ilotterytea
 * @version 1.6
 */
public enum EventType {
    /**
     * Custom event type.
     */
    CUSTOM("custom"),

    /**
     * "Stream live" event type.
     */
    LIVE("live"),

    /**
     * "Stream offline" event type.
     */
    OFFLINE("offline");

    private final String name;

    EventType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Optional<EventType> findEventTypeById(String id) {
        for (EventType eventType : EventType.values()) {
            if (eventType.name.equals(id)) {
                return Optional.of(eventType);
            }
        }

        return Optional.empty();
    }
}
