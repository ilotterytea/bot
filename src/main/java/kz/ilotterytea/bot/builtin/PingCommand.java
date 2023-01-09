package kz.ilotterytea.bot.builtin;

import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.api.permissions.Permissions;
import kz.ilotterytea.bot.models.ArgumentsModel;
import kz.ilotterytea.bot.utils.StringUtils;

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

        return String.format(
                "DankCrouching \u2615 Java %s \u00b7 Uptime: %s \u00b7 Used memory: %s%% (%sMB) of %sMB \u00b7 TMI: %sms \u00b7 7TV EventAPI: %s",
                System.getProperty("java.version"),
                ut,
                Math.round(percentMemUsage),
                Math.round(usedMemMb),
                Math.round(totalMemMb),
                Huinyabot.getInstance().getClient().getChat().getLatency(),
                (Huinyabot.getInstance().getSevenTVWSClient().isClosed()) ? "DISCONNECTED" : "CONNECTED"
        );
    }
}
