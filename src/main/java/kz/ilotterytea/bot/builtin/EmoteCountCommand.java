package kz.ilotterytea.bot.builtin;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.SharedConstants;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.api.permissions.Permissions;
import kz.ilotterytea.bot.i18n.LineIds;
import kz.ilotterytea.bot.models.ArgumentsModel;
import kz.ilotterytea.bot.models.serverresponse.Emote;
import kz.ilotterytea.bot.models.serverresponse.ServerPayload;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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

        if (s[0].length() == 0) {
            return Huinyabot.getInstance().getLocale().formattedText(
                    m.getLanguage(),
                    LineIds.C_ECOUNT_NOEMOTEPROVIDED,
                    Huinyabot.getInstance().getLocale().literalText(
                            m.getLanguage(),
                            LineIds.STV
                    )
            );
        }

        String name = s[0];
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .get()
                .url(SharedConstants.STATS_URL + "/api/v1/channel/" + m.getEvent().getChannel().getId() + "/emotes")
                .build();

        ArrayList<Emote> emotes;

        try (Response response = client.newCall(request).execute()) {
            if (response.code() != 200) {
                return Huinyabot.getInstance().getLocale().formattedText(
                        m.getLanguage(),
                        LineIds.HTTP_ERROR,
                        String.valueOf(response.code()),
                        "Stats API"
                );
            }

            if (response.body() == null) {
                return Huinyabot.getInstance().getLocale().formattedText(
                        m.getLanguage(),
                        LineIds.SOMETHING_WENT_WRONG
                );
            }

            String body = response.body().string();

            ServerPayload<ArrayList<Emote>> payload = new Gson().fromJson(body, new TypeToken<ServerPayload<ArrayList<Emote>>>(){}.getType());

            if (payload.getData() != null) {
                emotes = payload.getData();
            } else {
                return Huinyabot.getInstance().getLocale().formattedText(
                        m.getLanguage(),
                        LineIds.C_ETOP_NOCHANNELEMOTES,
                        Huinyabot.getInstance().getLocale().literalText(
                                m.getLanguage(),
                                LineIds.STV
                        ),
                        Huinyabot.getInstance().getLocale().literalText(
                                m.getLanguage(),
                                LineIds.STV
                        )
                );
            }
        } catch (IOException e) {
            return Huinyabot.getInstance().getLocale().formattedText(
                    m.getLanguage(),
                    LineIds.SOMETHING_WENT_WRONG
            );
        }

        if (emotes.isEmpty()) {
            return Huinyabot.getInstance().getLocale().formattedText(
                    m.getLanguage(),
                    LineIds.C_ETOP_NOCHANNELEMOTES,
                    Huinyabot.getInstance().getLocale().literalText(
                            m.getLanguage(),
                            LineIds.STV
                    ),
                    Huinyabot.getInstance().getLocale().literalText(
                            m.getLanguage(),
                            LineIds.STV
                    )
            );
        }

        // Get the emote:
        Optional<Emote> optionalEmote = emotes.stream().filter(e -> e.getName().equals(name) && e.getDeletionTimestamp() == null).findFirst();

        if (optionalEmote.isEmpty()) {
            return Huinyabot.getInstance().getLocale().formattedText(
                    m.getLanguage(),
                    LineIds.C_ECOUNT_NOEMOTEFOUND,
                    Huinyabot.getInstance().getLocale().literalText(
                            m.getLanguage(),
                            LineIds.STV
                    ),
                    name
            );
        }

        Emote emote = optionalEmote.get();

        // Sort the emote list:
        emotes.sort(Comparator.comparingInt(Emote::getUsedTimes));
        Collections.reverse(emotes);

        int position = emotes.indexOf(emote);

        return Huinyabot.getInstance().getLocale().formattedText(
                m.getLanguage(),
                LineIds.C_ECOUNT_SUCCESS,
                Huinyabot.getInstance().getLocale().literalText(
                        m.getLanguage(),
                        LineIds.STV
                ),
                emote.getName(),
                (emote.getGlobal() ? " *" : ""),
                String.valueOf(emote.getUsedTimes()),
                (position < 0 ? "N/A" : String.valueOf(position + 1)),
                String.valueOf(emotes.size())
        );
    }
}
