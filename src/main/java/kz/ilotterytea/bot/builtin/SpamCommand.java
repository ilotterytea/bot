package kz.ilotterytea.bot.builtin;

import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.api.permissions.Permissions;
import kz.ilotterytea.bot.models.ArgumentsModel;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Spam command.
 * @author ilotterytea
 * @since 1.0
 */
public class SpamCommand extends Command {
    @Override
    public String getNameId() { return "spam"; }

    @Override
    public int getDelay() { return 30000; }

    @Override
    public Permissions getPermissions() { return Permissions.MOD; }

    @Override
    public ArrayList<String> getOptions() { return new ArrayList<>(Arrays.asList("count")); }

    @Override
    public ArrayList<String> getSubcommands() { return new ArrayList<>(); }

    @Override
    public ArrayList<String> getAliases() { return new ArrayList<>(Arrays.asList("спам", "насрать", "repeat", "cv", "paste", "cvpaste")); }

    @Override
    public String run(ArgumentsModel m) {
        final int MAX_COUNT = 8;
        ArrayList<String> s = new ArrayList<>(Arrays.asList(m.getMessage().getMessage().split(" ")));

        if (s.size() <= 1) {
            return "No message found.";
        }
        int count;

        try {
            count = Integer.parseInt(s.get(0));
            s.remove(0);
        } catch (NumberFormatException e) {
            return "No spam count found.";
        }

        if (count > MAX_COUNT) {
            count = MAX_COUNT;
        }

        for (int i = 0; i < count; i++) {
            Huinyabot.getInstance().getClient().getChat().sendMessage(
                    m.getEvent().getChannel().getName(),
                    String.format(
                            "%s %s",
                            String.join(" ", s),
                            (m.getMessage().getOptions().contains("count")) ? i + 1 : ""
                    )
            );
        }

        return null;
    }
}
