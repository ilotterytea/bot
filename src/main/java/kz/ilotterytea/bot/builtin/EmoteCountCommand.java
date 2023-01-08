package kz.ilotterytea.bot.builtin;

import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.api.permissions.Permissions;
import kz.ilotterytea.bot.models.ArgumentsModel;
import kz.ilotterytea.bot.models.TargetModel;
import kz.ilotterytea.bot.models.emotes.Emote;
import kz.ilotterytea.bot.models.emotes.Provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * Emote count command.
 * @author ilotterytea
 * @since 1.1
 */
public class EmoteCountCommand extends Command {
    @Override
    public String getNameId() { return "ecount"; }

    @Override
    public int getDelay() { return 5000; }

    @Override
    public Permissions getPermissions() { return Permissions.USER; }

    @Override
    public ArrayList<String> getOptions() { return new ArrayList<>(); }

    @Override
    public ArrayList<String> getSubcommands() { return new ArrayList<>(); }

    @Override
    public ArrayList<String> getAliases() { return new ArrayList<>(Arrays.asList("count", "emote", "колво", "кол-во", "эмоут")); }

    @Override
    public String run(ArgumentsModel m) {
        String[] s = m.getMessage().getMessage().split(" ");

        if (s.length == 0) {
            return "[7TV] No emote provided.";
        }

        String name = s[0];
        TargetModel target = Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId());

        if (!target.getEmotes().containsKey(Provider.SEVENTV)) {
            return "[7TV] The 7TV emotes were not detected.";
        }

        Map<String, Emote> emotes = target.getEmotes().get(Provider.SEVENTV);

        for (Emote em : emotes.values()) {
            if (Objects.equals(em.getName(), name)) {
                return String.format(
                        "[7TV] %s%s has been used %s times.",
                        em.getName(),
                        (em.isDeleted()) ? "*" : "",
                        em.getCount()
                );
            }
        }

        return "[7TV] Emote \""+name+"\" is not found in the database.";
    }
}
