package kz.ilotterytea.bot.builtin;

import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.SharedConstants;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.api.permissions.Permissions;
import kz.ilotterytea.bot.i18n.LineIds;
import kz.ilotterytea.bot.models.ArgumentsModel;
import kz.ilotterytea.bot.utils.StringUtils;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Ping command.
 * @author ilotterytea
 * @since 1.0
 */
public class PingCommand extends Command {
    @Override
    public String getNameId() { return "ping"; }

    @Override
    public int getDelay() { return 5000; }

    @Override
    public Permissions getPermissions() { return Permissions.USER; }

    @Override
    public ArrayList<String> getOptions() { return new ArrayList<>(); }

    @Override
    public ArrayList<String> getSubcommands() { return new ArrayList<>(); }

    @Override
    public ArrayList<String> getAliases() { return new ArrayList<>(Arrays.asList("pong", "пинг", "понг")); }

    @Override
    public String run(ArgumentsModel m) {
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();

        String ut = StringUtils.formatTimestamp(uptime / 1000);

        Runtime rt = Runtime.getRuntime();
        double usedMemMb = ((rt.totalMemory() - rt.freeMemory()) / 1024.0) / 1024.0;
        double totalMemMb = (rt.totalMemory() / 1024.0) / 1024.0;
        double percentMemUsage = Math.round((usedMemMb / totalMemMb) * 100.0);

        String neurobajStatus;

        OkHttpClient client = new OkHttpClient.Builder().build();

        Request request = new Request.Builder()
                .get()
                .url(SharedConstants.NEUROBAJ_URL + "/api/v1/status")
                .build();

        Call call = client.newCall(request);

        Response response;

        try {
            response = call.execute();

            long ping = response.receivedResponseAtMillis() - response.sentRequestAtMillis();

            if (response.code() == 200) {
                neurobajStatus = "OK (" + ping + "ms)";
            } else {
                neurobajStatus = "NOT OK (" + response.code() + ")";
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            neurobajStatus = "NOT OK";
        }

        return Huinyabot.getInstance().getLocale().formattedText(
                m.getLanguage(),
                LineIds.C_PING_SUCCESS,
                System.getProperty("java.version"),
                ut,
                String.valueOf(Math.round(percentMemUsage)),
                String.valueOf(Math.round(usedMemMb)),
                String.valueOf(Math.round(totalMemMb)),
                String.valueOf(Huinyabot.getInstance().getClient().getChat().getLatency()),
                (Huinyabot.getInstance().getSevenTVWSClient().isClosed()) ?
        Huinyabot.getInstance().getLocale().literalText(
                m.getLanguage(),
                LineIds.DISCON
        ):Huinyabot.getInstance().getLocale().literalText(
                        m.getLanguage(),
                        LineIds.CON
                ),
                neurobajStatus
        );
    }
}
