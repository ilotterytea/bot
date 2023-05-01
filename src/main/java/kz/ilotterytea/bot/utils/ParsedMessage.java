package kz.ilotterytea.bot.utils;

import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.api.commands.Command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Parsed message.
 * @author ilotterytea
 * @version 1.4
 */
public class ParsedMessage {
    /**
     * Command ID.
     */
    private final String commandId;
    /**
     * Subcommand ID.
     */
    private final Optional<String> subcommandId;
    /**
     * The options used in the message.
     */
    private final List<String> usedOptions;
    /**
     * The rest of the message that has not been parsed.
     */
    private final Optional<String> message;

    /**
     * Split the message into a command, a subcommand, a setting, and the rest of the message.
     * @param rawMessage Raw message.
     * @param prefix Command prefix.
     * @return ParsedMessage if command (or alias) exists, otherwise null.
     */
    public static Optional<ParsedMessage> parse(String rawMessage, String prefix) {
        ArrayList<String> s = new ArrayList<>(List.of(rawMessage.split(" ")));

        if (!s.get(0).startsWith(prefix)) {
            return Optional.empty();
        }

        // Getting the command:
        String commandId = s.get(0).substring(prefix.length());
        s.remove(0);

        if (commandId.isBlank()) {
            return Optional.empty();
        }

        Optional<Command> optionalCommand = Huinyabot.getInstance().getLoader().getCommand(commandId);

        if (optionalCommand.isEmpty()) {
            return Optional.empty();
        }

        Command command = optionalCommand.get();
        commandId = command.getNameId();

        // Return if only a command without anything was used:
        if (s.isEmpty()) {
            return Optional.of(new ParsedMessage(
                    commandId,
                    Optional.empty(),
                    Collections.emptyList(),
                    Optional.empty()
            ));
        }

        // Getting the subcommand:
        String subcommandId;

        if (!command.getSubcommands().contains(s.get(0))) {
            subcommandId = s.get(0);
            s.remove(0);
        } else {
            subcommandId = null;
        }

        // Parsing the options:
        final String OPTIONS_PREFIX = "--";
        List<String> options = new ArrayList<>();

        for (String word : s) {
            if (word.startsWith(OPTIONS_PREFIX) && word.length() > OPTIONS_PREFIX.length()) {
                options.add(word.substring(OPTIONS_PREFIX.length()));
            }
        }

        // Removing options from the message array:
        for (String option : options) {
            s.remove(OPTIONS_PREFIX + option);
        }

        String finalMessage = String.join(" ", s).trim();

        return Optional.of(
                new ParsedMessage(
                        commandId,
                        Optional.ofNullable(subcommandId),
                        options,
                        Optional.ofNullable((finalMessage.isBlank()) ? null : finalMessage)
                )
        );
    }

    public ParsedMessage(
            String commandId,
            Optional<String> subcommandId,
            List<String> usedOptions,
            Optional<String> message
    ) {
        this.commandId = commandId;
        this.subcommandId = subcommandId;
        this.usedOptions = usedOptions;
        this.message = message;
    }

    public String getCommandId() {
        return commandId;
    }

    public Optional<String> getSubcommandId() {
        return subcommandId;
    }

    public List<String> getUsedOptions() {
        return usedOptions;
    }

    public Optional<String> getMessage() {
        return message;
    }
}
