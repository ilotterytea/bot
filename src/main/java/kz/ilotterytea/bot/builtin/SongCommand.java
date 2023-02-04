package kz.ilotterytea.bot.builtin;

import jdash.client.exception.GDClientException;
import jdash.common.entity.GDSong;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.api.permissions.Permissions;
import kz.ilotterytea.bot.models.ArgumentsModel;

import java.util.*;

/**
 * Song command.
 * @author ilotterytea
 * @since 1.3
 */
public class SongCommand extends Command {
    @Override
    public String getNameId() { return "song"; }

    @Override
    public int getDelay() { return 10000; }

    @Override
    public Permissions getPermissions() { return Permissions.USER; }

    @Override
    public ArrayList<String> getOptions() { return new ArrayList<>(); }

    @Override
    public ArrayList<String> getSubcommands() { return new ArrayList<>(); }

    @Override
    public ArrayList<String> getAliases() { return new ArrayList<>(Collections.singleton("s")); }

    @Override
    public String run(ArgumentsModel m) {
        GDSong song = null;
        ArrayList<String> s = new ArrayList<>(Arrays.asList(m.getMessage().getMessage().split(" ")));
        final long MAX_ID_RANGE = 1_000_000L;
        long id;

        if (!Objects.equals(s.get(0), "")) {
            try {
                id = Integer.parseInt(s.get(0));
            } catch (NumberFormatException e) {
                return "The provided ID isn't an integer.";
            }

            try {
                song = Huinyabot.getInstance().getGDClient().getSongInfo(id).block();
            } catch (GDClientException e) {
                e.printStackTrace();
            }

            if (song == null) {
                return "The Song ID " + id + " not found!";
            }
        } else {
            do {
                id = Math.round(Math.floor(Math.random() * MAX_ID_RANGE));

                try {
                    song = Huinyabot.getInstance().getGDClient().getSongInfo(id).block();
                } catch (GDClientException e) {
                    System.out.println(e.getMessage());
                }
            } while (song == null);
        }

        return String.format(
                "\uD83C\uDFB5 \"%s\" by %s (ID %s) - %s",
                song.title(),
                song.artist(),
                song.id(),
                "https://www.newgrounds.com/audio/listen/" + id
        );
    }
}
