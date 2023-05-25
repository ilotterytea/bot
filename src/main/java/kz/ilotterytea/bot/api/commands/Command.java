package kz.ilotterytea.bot.api.commands;

import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import kz.ilotterytea.bot.entities.channels.Channel;
import kz.ilotterytea.bot.entities.permissions.Permission;
import kz.ilotterytea.bot.entities.permissions.UserPermission;
import kz.ilotterytea.bot.entities.users.User;
import kz.ilotterytea.bot.utils.ParsedMessage;
import org.hibernate.Session;

import java.util.*;

/**
 * Command.
 * @author ilotterytea
 * @since 1.0
 */
public interface Command {
    /**
     * Get the name ID of command.
     * @since 1.0
     * @author ilotterytea
     * @return name ID.
     */
    String getNameId();
    /**
     * Get the seconds delay between command executions.
     * @since 1.0
     * @author ilotterytea
     * @return delay.
     */
    int getDelay();
    /**
     * Get the ID of minimal permissions to run the command.
     * @return permission ID.
     */
    Permission getPermissions();
    /**
     * Get the names of the options that should be used in the command.
     * @return array list of option names.
     */
    List<String> getOptions();
    /**
     * Get the names of the subcommands that should be used in the command.
     * @return array list of subcommand names.
     */
    List<String> getSubcommands();
    /**
     * Get command alias names.
     * @return array list of alias names.
     */
    List<String> getAliases();
    /**
     * Run the command.
     * @since 1.0
     * @author ilotterytea
     * @return response.
     */
    Optional<String> run(
            Session session,
            IRCMessageEvent event,
            ParsedMessage message,
            Channel channel,
            User user,
            UserPermission permission
    );
}
