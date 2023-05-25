package kz.ilotterytea.bot.entities.events;

import java.util.Optional;

/**
 * Flags for events.
 * @author ilotterytea
 * @version 1.6
 */
public enum EventFlag {
    /**
     * A flag for massping.
     * With this flag, a list of chatters will be added to the list of usernames.
     */
    MASSPING("massping"),

    /**
     * Non-subscription flag.
     * Forbids subscribing to an event with this flag.
     */
    NON_SUBSCRIPTION("non_subscription");

    private final String name;

    EventFlag(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Optional<EventFlag> findEventFlagById(String id) {
        for (EventFlag eventFlag : EventFlag.values()) {
            if (eventFlag.name.equals(id)) {
                return Optional.of(eventFlag);
            }
        }

        return Optional.empty();
    }
}
