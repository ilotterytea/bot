package kz.ilotterytea.bot.api.commands;

import kz.ilotterytea.bot.api.permissions.Permissions;
import kz.ilotterytea.bot.models.ArgumentsModel;

import java.util.ArrayList;

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
     * Get the names of the options that should be used in the command.
     * @return array list of option names.
     */
    public abstract ArrayList<String> getOptions();
    /**
     * Get the names of the subcommands that should be used in the command.
     * @return array list of subcommand names.
     */
    public abstract ArrayList<String> getSubcommands();
    /**
     * Get command alias names.
     * @return array list of alias names.
     */
    public abstract ArrayList<String> getAliases();
    /**
     * Run the command.
     * @since 1.0
     * @author ilotterytea
     * @return response.
     */
    public abstract String run(ArgumentsModel args);
}
