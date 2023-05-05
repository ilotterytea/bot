package kz.ilotterytea.bot.api.commands;

import kz.ilotterytea.bot.entities.channels.Channel;
import kz.ilotterytea.bot.entities.permissions.UserPermission;
import kz.ilotterytea.bot.entities.users.User;
import kz.ilotterytea.bot.utils.ParsedMessage;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.twitch4j.chat.events.channel.IRCMessageEvent;

import java.util.*;

/**
 * Command loader.
 * @author ilotterytea
 * @since 1.0
 */
public class CommandLoader extends ClassLoader {
    private final Map<String, Command> COMMANDS;
    private final Logger LOGGER = LoggerFactory.getLogger(CommandLoader.class);

    public CommandLoader() {
        super();
        COMMANDS = new HashMap<>();
        init();
    }

    private void init() {
        Reflections reflections = new Reflections("kz.ilotterytea.bot.builtin");

        Set<Class<? extends Command>> classes = reflections.getSubTypesOf(Command.class);

        for (Class<? extends Command> clazz : classes) {
            try {
                register(clazz.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Register the command.
     * @since 1.0
     * @author ilotterytea
     * @param command Command.
     */
    public void register(Command command) {
        COMMANDS.put(command.getNameId(), command);
        LOGGER.debug(String.format("Successfully loaded the %s command!", command.getNameId()));
    }

    /**
     * Call the command.
     * @since 1.0
     * @author ilotterytea
     * @param nameId Command name ID.
     * @param args Arguments.
     * @return response
     */
    public Optional<String> call(String nameId, IRCMessageEvent event, ParsedMessage message, Channel channel, User user, UserPermission permission) {
        Optional<String> response = Optional.empty();

        if (COMMANDS.containsKey(nameId)) {
            Command cmd = COMMANDS.get(nameId);
            if (permission.getPermission().getValue() >= cmd.getPermissions().getValue()) {
                try {
                    response = cmd.run(event, message, channel, user, permission);
                } catch (Exception e) {
                    LOGGER.error(String.format("Error occurred while running the %s command", nameId), e);
                }
            }
        }

        return response;
    }

    public Optional<Command> getCommand(String id) {
        return this.COMMANDS.values().stream().filter(c -> c.getNameId().equals(id) || c.getAliases().contains(id)).findFirst();
    }

    /**
     * Get the loaded commands.
     * @since 1.0
     * @author ilotterytea
     * @return a map of the commands.
     */
    public Map<String, Command> getCommands() { return COMMANDS; }
}
