package kz.ilotterytea.bot.models;

import java.util.*;

/**
 * Message model.
 * @author ilotterytea
 * @since 1.0
 */
public class MessageModel {
    private final String command;
    private final String subCommand;
    private final ArrayList<String> options;
    private final String message;
    private final String rawMessage;

    public MessageModel(
            String command,
            String subCommand,
            ArrayList<String> options,
            String message,
            String rawMessage
    ) {
        this.command = command;
        this.subCommand = subCommand;
        this.options = options;
        this.message = message;
        this.rawMessage = rawMessage;
    }

    public static MessageModel create(String rawMessage, String prefix) {
        ArrayList<String> s = new ArrayList<>(Arrays.asList(rawMessage.split(" ")));

        String c = null;
        String sc = null;
        ArrayList<String> o = new ArrayList<>();
        String m;

        // Options:
        for (String w : s) {
            if (w.startsWith("--") && w.length() > 2) {
                o.add(w.substring("--".length()));
            }
        }

        // Removing options from the split message:
        for (String w : o) {
            s.remove("--" + w);
        }

        // Command:
        if (s.get(0) != null) {
            c = s.get(0).substring(prefix.length());
            s.remove(0);
        }

        // Subcommand:
        if (s.size() >= 1 && s.get(0) != null && !s.get(0).startsWith("--")) {
            sc = s.get(0);
            s.remove(0);
        }

        // Building the clear message:
        m = String.join(" ", s);

        return new MessageModel(c, sc, o, m, rawMessage);
    }

    public String getCommand() { return command; }
    public String getSubCommand() { return subCommand; }
    public ArrayList<String> getOptions() { return options; }
    public String getMessage() { return message; }
    public String getRawMessage() { return rawMessage; }
}
