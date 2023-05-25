package kz.ilotterytea.bot.builtin.user;

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
import kz.ilotterytea.bot.models.serverresponse.ivr.UserInfo;
import kz.ilotterytea.bot.utils.ParsedMessage;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.hibernate.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A command for checking if user is banned.
 * @author ilotterytea
 * @since 1.5
 */
public class UserIdCheckCommand implements Command {
    @Override
    public String getNameId() { return "userid"; }

    @Override
    public int getDelay() { return 5000; }

    @Override
    public Permission getPermissions() { return Permission.USER; }

    @Override
    public List<String> getOptions() { return Collections.singletonList("login"); }

    @Override
    public List<String> getSubcommands() { return Collections.emptyList(); }

    @Override
    public List<String> getAliases() { return List.of("uid", "isbanned"); }

    @Override
    public Optional<String> run(Session session, IRCMessageEvent event, ParsedMessage message, Channel channel, User user, UserPermission permission) {
        if (message.getMessage().isEmpty()) {
            return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
                    channel.getPreferences().getLanguage(),
                    LineIds.NO_MESSAGE
            ));
        }

        // Parsing ids and logins:
        String[] s = message.getMessage().get().split(",");
        ArrayList<String> loginQuery = new ArrayList<>();
        ArrayList<String> idQuery = new ArrayList<>();

        for (String id : s) {
            id = id.trim();

            try {
                int parsedId = Integer.parseInt(id);
                idQuery.add(id);
            } catch (NumberFormatException e) {
                loginQuery.add(id);
            }
        }

        // Building the query:
        String query = "?";

        if (!idQuery.isEmpty()) {
            query = query + "id=" + String.join(",", idQuery);
        }

        if (!loginQuery.isEmpty()) {
            if (query.length() > 1) {
                query = query + "&";
            }

            query = query + "login=" + String.join(",", loginQuery);
        }

        // Requesting:
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .get()
                .url(SharedConstants.IVR_USER_ENDPOINT + query)
                .build();

        ArrayList<String> results = new ArrayList<>();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() == null || response.code() != 200) {
                return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.HTTP_ERROR,
                        String.valueOf(response.code()),
                        "IVR API"
                ));
            }

            List<UserInfo> userInfos = new Gson().fromJson(response.body().string(), new TypeToken<List<UserInfo>>(){}.getType());

            if (userInfos.isEmpty()) {
                return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
                        channel.getPreferences().getLanguage(),
                        LineIds.NO_TWITCH_USER
                ));
            }

            for (UserInfo userInfo : userInfos) {
                results.add(String.format(
                        "%s: %s %s",
                        (message.getUsedOptions().contains("login")) ? userInfo.getLogin() : userInfo.getDisplayName(),
                        userInfo.getId(),
                        (userInfo.getBanned()) ? ("[⛔ " + userInfo.getBanReason() + "]") : ""
                        ));
            }

        } catch (IOException e) {
            return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
                    channel.getPreferences().getLanguage(),
                    LineIds.SOMETHING_WENT_WRONG
            ));
        }

        return Optional.of(String.join(" | ", results));
    }
}
