package kz.ilotterytea.bot.builtin.emotes;

import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.SharedConstants;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.entities.channels.Channel;
import kz.ilotterytea.bot.entities.permissions.Permission;
import kz.ilotterytea.bot.entities.permissions.UserPermission;
import kz.ilotterytea.bot.entities.users.User;
import kz.ilotterytea.bot.i18n.LineIds;
import kz.ilotterytea.bot.models.serverresponse.Emote;
import kz.ilotterytea.bot.models.serverresponse.ServerPayload;
import kz.ilotterytea.bot.utils.ParsedMessage;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.hibernate.Session;

import java.io.IOException;
import java.util.*;

/**
 * Emote count command.
 * @author ilotterytea
 * @since 1.1
 */
public class EmoteCountCommand implements Command {
    @Override
    public String getNameId() { return "ecount"; }

    @Override
    public int getDelay() { return 5000; }

    @Override
    public Permission getPermissions() { return Permission.USER; }

    @Override
    public List<String> getOptions() { return Collections.emptyList(); }

    @Override
    public List<String> getSubcommands() { return Collections.emptyList(); }

    @Override
    public List<String> getAliases() { return List.of("count", "emote", "колво", "кол-во", "эмоут"); }

    @Override
    public Optional<String> run(Session session, IRCMessageEvent event, ParsedMessage message, Channel channel, User user, UserPermission permission) {
        if (message.getMessage().isEmpty()) {
            return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                    channel.getPreferences().getLanguage(),
                    LineIds.C_ECOUNT_NOEMOTEPROVIDED,
                    Huinyabot.getInstance().getLocale().literalText(
                            channel.getPreferences().getLanguage(),
                            LineIds.STV
                    )
            ));
        }
        
        String[] s = message.getMessage().get().split(" ");

        String name = s[0];
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .get()
                .url(SharedConstants.STATS_URL + "/api/v1/channel/" + channel.getAliasId() + "/emotes")
                .build();

        ArrayList<Emote> emotes;

        try (Response response = client.newCall(request).execute()) {
            if (response.code() != 200) {
            	return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.HTTP_ERROR,
                        String.valueOf(response.code()),
                        "Stats API"
                ));
            }

            if (response.body() == null) {
            	return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.SOMETHING_WENT_WRONG
                ));
            }

            String body = response.body().string();

            ServerPayload<ArrayList<Emote>> payload = new Gson().fromJson(body, new TypeToken<ServerPayload<ArrayList<Emote>>>(){}.getType());

            if (payload.getData() != null) {
                emotes = payload.getData();
            } else {
            	return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_ETOP_NOCHANNELEMOTES,
                        Huinyabot.getInstance().getLocale().literalText(
                                channel.getPreferences().getLanguage(),
                                LineIds.STV
                        ),
                        Huinyabot.getInstance().getLocale().literalText(
                                channel.getPreferences().getLanguage(),
                                LineIds.STV
                        )
                ));
            }
        } catch (IOException e) {
        	return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                    channel.getPreferences().getLanguage(),
                    LineIds.SOMETHING_WENT_WRONG
            ));
        }

        if (emotes.isEmpty()) {
        	return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                    channel.getPreferences().getLanguage(),
                    LineIds.C_ETOP_NOCHANNELEMOTES,
                    Huinyabot.getInstance().getLocale().literalText(
                            channel.getPreferences().getLanguage(),
                            LineIds.STV
                    ),
                    Huinyabot.getInstance().getLocale().literalText(
                            channel.getPreferences().getLanguage(),
                            LineIds.STV
                    )
            ));
        }

        // Get the emote:
        Optional<Emote> optionalEmote = emotes.stream().filter(e -> e.getName().equals(name) && e.getDeletionTimestamp() == null).findFirst();

        if (optionalEmote.isEmpty()) {
        	return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                    channel.getPreferences().getLanguage(),
                    LineIds.C_ECOUNT_NOEMOTEFOUND,
                    Huinyabot.getInstance().getLocale().literalText(
                            channel.getPreferences().getLanguage(),
                            LineIds.STV
                    ),
                    name
            ));
        }

        Emote emote = optionalEmote.get();

        // Sort the emote list:
        emotes.sort(Comparator.comparingInt(Emote::getUsedTimes));
        Collections.reverse(emotes);

        int position = emotes.indexOf(emote);

        return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                channel.getPreferences().getLanguage(),
                LineIds.C_ECOUNT_SUCCESS,
                Huinyabot.getInstance().getLocale().literalText(
                        channel.getPreferences().getLanguage(),
                        LineIds.STV
                ),
                emote.getName(),
                (emote.getGlobal() ? " *" : ""),
                String.valueOf(emote.getUsedTimes()),
                (position < 0 ? "N/A" : String.valueOf(position + 1)),
                String.valueOf(emotes.size())
        ));
    }
}
