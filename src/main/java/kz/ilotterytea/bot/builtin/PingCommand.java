package kz.ilotterytea.bot.builtin;

import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.SharedConstants;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.entities.channels.Channel;
import kz.ilotterytea.bot.entities.permissions.Permission;
import kz.ilotterytea.bot.entities.permissions.UserPermission;
import kz.ilotterytea.bot.entities.users.User;
import kz.ilotterytea.bot.i18n.LineIds;
import kz.ilotterytea.bot.thirdpartythings.seventv.eventapi.SevenTVEventAPIClient;
import kz.ilotterytea.bot.utils.ParsedMessage;
import kz.ilotterytea.bot.utils.StringUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.github.twitch4j.chat.events.channel.IRCMessageEvent;

/**
 * Ping command.
 * @author ilotterytea
 * @since 1.0
 */
public class PingCommand implements Command {
    @Override
    public String getNameId() { return "ping"; }

    @Override
    public int getDelay() { return 5000; }

    @Override
    public Permission getPermissions() { return Permission.USER; }

    @Override
    public List<String> getOptions() { return Collections.emptyList(); }

    @Override
    public List<String> getSubcommands() { return Collections.emptyList(); }

    @Override
    public List<String> getAliases() { return List.of("pong", "пинг", "понг"); }

    @Override
    public Optional<String> run(IRCMessageEvent event, ParsedMessage message, Channel channel, User user, UserPermission permission) {
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();

        String ut = StringUtils.formatTimestamp(uptime / 1000);

        Runtime rt = Runtime.getRuntime();
        double usedMemMb = ((rt.totalMemory() - rt.freeMemory()) / 1024.0) / 1024.0;
        double totalMemMb = (rt.totalMemory() / 1024.0) / 1024.0;
        double percentMemUsage = Math.round((usedMemMb / totalMemMb) * 100.0);

        OkHttpClient client = new OkHttpClient.Builder().build();

        // Getting info about Neurobaj:
        String neurobajStatus;

        try (Response response = client.newCall(new Request.Builder()
                .get()
                .url(SharedConstants.NEUROBAJ_URL + "/api/v1/status")
                .build()
        ).execute()) {
            if (response.code() != 200) {
                neurobajStatus = "NOT OK (" + response.code() + ")";
            } else {
                neurobajStatus = "OK (" + (response.receivedResponseAtMillis() - response.sentRequestAtMillis()) + "ms)";
            }
        } catch (IOException e) {
            e.printStackTrace();
            neurobajStatus = "N/A";
        }

        // Getting info about Stats:
        String statsStatus;

        try (Response response = client.newCall(new Request.Builder()
                .get()
                .url(SharedConstants.STATS_URL + "/api/v1/health")
                .build()
        ).execute()) {
            if (response.code() != 200) {
                statsStatus = "NOT OK (" + response.code() + ")";
            } else {
                statsStatus = "OK (" + (response.receivedResponseAtMillis() - response.sentRequestAtMillis()) + "ms)";
            }
        } catch (IOException e) {
            e.printStackTrace();
            statsStatus = "N/A";
        }

        return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                channel.getPreferences().getLanguage(),
                LineIds.C_PING_SUCCESS,
                System.getProperty("java.version"),
                ut,
                String.valueOf(Math.round(percentMemUsage)),
                String.valueOf(Math.round(usedMemMb)),
                String.valueOf(Math.round(totalMemMb)),
                String.valueOf(Huinyabot.getInstance().getClient().getChat().getLatency()),
                (SevenTVEventAPIClient.getInstance().isClosed()) ?
        Huinyabot.getInstance().getLocale().literalText(
                channel.getPreferences().getLanguage(),
                LineIds.DISCON
        ):Huinyabot.getInstance().getLocale().literalText(
                        channel.getPreferences().getLanguage(),
                        LineIds.CON
                ),
                neurobajStatus,
                statsStatus
        ));
    }
}
