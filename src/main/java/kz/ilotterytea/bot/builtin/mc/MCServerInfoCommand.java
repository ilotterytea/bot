package kz.ilotterytea.bot.builtin.mc;

import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import com.google.gson.Gson;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.SharedConstants;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.entities.channels.Channel;
import kz.ilotterytea.bot.entities.permissions.Permission;
import kz.ilotterytea.bot.entities.permissions.UserPermission;
import kz.ilotterytea.bot.entities.users.User;
import kz.ilotterytea.bot.i18n.LineIds;
import kz.ilotterytea.bot.models.serverresponse.mc.ServerInfo;
import kz.ilotterytea.bot.utils.ParsedMessage;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.hibernate.Session;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A command for getting info about Minecraft servers.
 * @author ilotterytea
 * @since 1.5
 */
public class MCServerInfoCommand implements Command {
    @Override
    public String getNameId() { return "mcserver"; }

    @Override
    public int getDelay() { return 5000; }

    @Override
    public Permission getPermissions() { return Permission.USER; }

    @Override
    public List<String> getOptions() { return Collections.emptyList(); }

    @Override
    public List<String> getSubcommands() { return Collections.emptyList(); }

    @Override
    public List<String> getAliases() { return List.of("mcsrv", "mcs"); }

    @Override
    public Optional<String> run(Session session, IRCMessageEvent event, ParsedMessage message, Channel channel, User user, UserPermission permission) {
        if (message.getMessage().isEmpty()) {
            return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
                    channel.getPreferences().getLanguage(),
                    LineIds.NO_MESSAGE
            ));
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .get()
                .url(SharedConstants.MCSRVSTATUS_ENDPOINT + "/" + message.getMessage().get())
                .build();

        try (Response response = client.newCall(request).execute()){
            if (response.body() == null || response.code() != 200) {
                return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.HTTP_ERROR,
                        String.valueOf(response.code()),
                        "MCSRVSTATUS"
                ));
            }

            ServerInfo serverInfo = new Gson().fromJson(response.body().string(), ServerInfo.class);

            if (!serverInfo.getOnline()) {
                return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_MCSERVER_SERVERISOFFLINE,
                        serverInfo.getHostname()
                ));
            }

            return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                    channel.getPreferences().getLanguage(),
                    LineIds.C_MCSERVER_SUCCESS,
                    serverInfo.getHostname(),
                    (serverInfo.getMotd().containsKey("clean")) ? String.join(" ~ ", serverInfo.getMotd().get("clean")) : "N/A",
                    (serverInfo.getPlayers().containsKey("online")) ? String.valueOf(serverInfo.getPlayers().get("online")) : "N/A",
                    (serverInfo.getPlayers().containsKey("max")) ? String.valueOf(serverInfo.getPlayers().get("max")) : "N/A",
                    serverInfo.getVersion()
            ));
        } catch (IOException e) {
            return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
                    channel.getPreferences().getLanguage(),
                    LineIds.SOMETHING_WENT_WRONG
            ));
        }

    }
}
