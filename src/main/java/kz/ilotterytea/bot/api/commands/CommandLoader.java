package kz.ilotterytea.bot.api.commands;

import kz.ilotterytea.bot.models.ArgumentsModel;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public String call(String nameId, ArgumentsModel args) {
        String response = null;

        if (COMMANDS.containsKey(nameId)) {
            Command cmd = COMMANDS.get(nameId);
            if (args.getCurrentPermissions().getId() >= cmd.getPermissions().getId()) {
                try {
                    response = cmd.run(args);
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
