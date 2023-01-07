package kz.ilotterytea.bot.api.commands;

import kz.ilotterytea.bot.api.permissions.Permissions;
import kz.ilotterytea.bot.models.ArgumentsModel;

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
     * Get the ID of minimal permissions to run the command.
     * @return permission ID.
     */
    public abstract Permissions getPermissions();
    /**
     * Run the command.
     * @since 1.0
     * @author ilotterytea
     * @return response.
     */
    public abstract String run(ArgumentsModel args);
}
