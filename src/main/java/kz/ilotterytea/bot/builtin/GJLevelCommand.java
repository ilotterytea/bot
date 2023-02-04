package kz.ilotterytea.bot.builtin;

import jdash.client.exception.GDClientException;
import jdash.common.LevelBrowseMode;
import jdash.common.entity.GDLevel;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.api.permissions.Permissions;
import kz.ilotterytea.bot.models.ArgumentsModel;
import kz.ilotterytea.bot.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

/**
 * 'Get a level from Geometry Dash' command.
 * @author ilotterytea
 * @since 1.3
 */
public class GJLevelCommand extends Command {
    @Override
    public String getNameId() { return "gdlevel"; }

    @Override
    public int getDelay() { return 10000; }

    @Override
    public Permissions getPermissions() { return Permissions.USER; }

    @Override
    public ArrayList<String> getOptions() { return new ArrayList<>(); }

    @Override
    public ArrayList<String> getSubcommands() { return new ArrayList<>(); }

    @Override
    public ArrayList<String> getAliases() { return new ArrayList<>(Collections.singleton("gdl")); }

    @Override
    public String run(ArgumentsModel m) {
        GDLevel level = null;
        ArrayList<String> s = new ArrayList<>(Arrays.asList(m.getMessage().getMessage().split(" ")));
        final long MAX_ID_RANGE = 87_000_000L;
        Long id = null;

        if (!Objects.equals(s.get(0), "")) {
            try {
                id = Long.parseLong(s.get(0));
            } catch (NumberFormatException e) {
                try {
                    level = Huinyabot.getInstance().getGDClient().browseLevels(
                            LevelBrowseMode.SEARCH,
                            String.join(" ", s),
                            null,
                            0
                    ).blockFirst();
                } catch (GDClientException ex){
                    ex.printStackTrace();
                    return "The level name \"" + s.get(0) + "\" not exist!";
                }
            }

            if (id != null) {
                try {
                    level = Huinyabot.getInstance().getGDClient().findLevelById(id).block();
                } catch (GDClientException e) {
                    e.printStackTrace();
                }
            }

            if (level == null) {
                return "The level ID " + id + " not found!";
            }
        } else {
            do {
                id = Math.round(Math.floor(Math.random() * MAX_ID_RANGE));

                try {
                    level = Huinyabot.getInstance().getGDClient().findLevelById(id).block();
                } catch (GDClientException e) {
                    System.out.println(e.getMessage());
                }
            } while (level == null);
        }

        level.demonDifficulty();

        return String.format(
                "\uD83D\uDFE6 \"%s\" by %s (ID %s) - %s %s (%s%s) - %s \uD83D\uDC4D %s ⬆ %s",
                level.name(),
                (level.creatorName().isPresent()) ? level.creatorName().get() : "N/A",
                level.id(),
                (level.isDemon()) ? level.demonDifficulty() : "",
                level.actualDifficulty(),
                (level.stars() > 0) ? level.stars() + " ⭐" : "N/A",
                (level.hasCoinsVerified() && level.coinCount() > 0) ? ", " + level.coinCount() + " \uD83E\uDE99" : "",
                StringUtils.formatNumber(level.likes()),
                StringUtils.formatNumber(level.downloads()),
                (level.song().isPresent()) ? String.format(
                        " - Song: \"%s\" by %s %s",
                        level.song().get().title(),
                        level.song().get().artist(),
                        (level.song().get().downloadUrl().isPresent()) ?
                                "[ ID " + level.song().get().id() + " - " +
                                        "https://www.newgrounds.com/audio/listen/" + level.song().get().id() + " ]"
                                : ""
                ) : ""
        );
    }
}
