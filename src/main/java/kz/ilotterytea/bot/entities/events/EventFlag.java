package kz.ilotterytea.bot.entities.events;

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
    MASSPING,

    /**
     * Non-subscription flag.
     * Forbids subscribing to an event with this flag.
     */
    NON_SUBSCRIPTION
}
