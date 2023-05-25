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
 * Emote top command.
 * @author ilotterytea
 * @since 1.1
 */
public class EmoteTopCommand implements Command {
    @Override
    public String getNameId() { return "etop"; }

    @Override
    public int getDelay() { return 10000; }

    @Override
    public Permission getPermissions() { return Permission.USER; }

    @Override
    public List<String> getOptions() { return Collections.emptyList(); }

    @Override
    public List<String> getSubcommands() { return Collections.emptyList(); }

    @Override
    public List<String> getAliases() { return List.of("emotetop", "топэмоутов"); }

    @Override
    public Optional<String> run(Session session, IRCMessageEvent event, ParsedMessage message, Channel channel, User user, UserPermission permission) {
        final int MAX_COUNT = 10;
        int count;

        if (message.getMessage().isEmpty()) {
            count = MAX_COUNT;
        } else {
        	String[] s = message.getMessage().get().split(" ");
        	
            try {
                count = Integer.parseInt(s[1]);
            } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                count = MAX_COUNT;
            }
        }

        if (count > MAX_COUNT) {
            count = MAX_COUNT;
        }

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

        // Remove the deleted emotes:
        emotes.removeIf(e -> e.getDeletionTimestamp() != null);

        // Sort the emotes by used count:
        emotes.sort(Comparator.comparingInt(Emote::getUsedTimes));
        Collections.reverse(emotes);

        if (emotes.size() < count) {
            count = emotes.size();
        }

        ArrayList<String> msgs = new ArrayList<>();

        msgs.add("");
        int index = 0;

        for (int i = 0; i < count; i++) {
            Emote em = emotes.get(i);

            StringBuilder sb = new StringBuilder();

            if (
                    Huinyabot.getInstance().getLocale().formattedText(
                            channel.getPreferences().getLanguage(),
                            LineIds.C_ETOP_SUCCESS,
                            Huinyabot.getInstance().getLocale().literalText(
                                    channel.getPreferences().getLanguage(),
                                    LineIds.STV
                            ),
                            msgs.get(index) + (i + 1) + ". " + em.getName()
                            + (em.getGlobal() ? " *" : "")
                            + " (" + em.getUsedTimes() + "); "
                    ).length() < 500
            ) {
                sb.append(msgs.get(index))
                        .append(i + 1)
                        .append(". ")
                        .append(em.getName())
                        .append(em.getGlobal() ? " ^" : "")
                        .append(" (")
                        .append(em.getUsedTimes())
                        .append("); ");
            } else {
                msgs.add("");
                index++;
            }

            msgs.remove(index);
            msgs.add(index, sb.toString());
        }

        for (String msg : msgs) {
            Huinyabot.getInstance().getClient().getChat().sendMessage(
                    channel.getAliasName(),
                    Huinyabot.getInstance().getLocale().formattedText(
                            channel.getPreferences().getLanguage(),
                            LineIds.C_ETOP_SUCCESS,
                            Huinyabot.getInstance().getLocale().literalText(
                                    channel.getPreferences().getLanguage(),
                                    LineIds.STV
                            ),
                            msg
                    ),
                    null,
                    (event.getMessageId().isPresent()) ? event.getMessageId().get() : null
            );
        }

        return null;
    }
}
