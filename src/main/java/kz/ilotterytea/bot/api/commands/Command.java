package kz.ilotterytea.bot.api.commands;

import com.github.twitch4j.chat.events.channel.IRCMessageEvent;

/**
 * Command.
 * @author ilotterytea
 * @since 1.0
 */
public abstract class Command {
    /**
     * Get the name ID of command.
     * @since 1.0
     * @author ilotterytea
     * @return name ID.
     */
    public abstract String getNameId();
    /**
     * Get the seconds delay between command executions.
     * @since 1.0
     * @author ilotterytea
     * @return delay.
     */
    public abstract int getDelay();
    /**
     * Run the command.
     * @since 1.0
     * @author ilotterytea
     * @return response.
     */
    public abstract String run(IRCMessageEvent ev);
}
