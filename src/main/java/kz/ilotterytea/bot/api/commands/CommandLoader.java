package kz.ilotterytea.bot.api.commands;

import kz.ilotterytea.bot.models.ArgumentsModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream("kz/ilotterytea/bot/builtin");
        assert stream != null;
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));

        Set<String> set = br.lines()
                .filter(line -> line.endsWith(".class"))
                .map(l -> l.substring(0, l.lastIndexOf('.')))
                .collect(Collectors.toSet());
        for (String clazz : set) {
            register(clazz);
        }
    }

    /**
     * Register the command.
     * @since 1.0
     * @author ilotterytea
     * @param nameId Command name ID.
     */
    public void register(String nameId) {
        try {
            Class<Command> c = (Class<Command>) Class.forName("kz.ilotterytea.bot.builtin." + nameId, true, super.getParent());
            Command cmd = c.newInstance();

            COMMANDS.put(cmd.getNameId(), cmd);
            LOGGER.debug(String.format("Successfully loaded the %s command!", cmd.getNameId()));
        } catch (ClassNotFoundException e) {
            LOGGER.error(String.format("Error occurred while registering the %s command: ", nameId), e);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
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
            if (args.getSender().getPermissions().getId() >= cmd.getPermissions().getId()) {
                try {
                    response = cmd.run(args);
                } catch (Exception e) {
                    LOGGER.error(String.format("Error occurred while running the %s command", nameId), e);
                }
            }
        }

        return response;
    }

    /**
     * Get the loaded commands.
     * @since 1.0
     * @author ilotterytea
     * @return a map of the commands.
     */
    public Map<String, Command> getCommands() { return COMMANDS; }
}
