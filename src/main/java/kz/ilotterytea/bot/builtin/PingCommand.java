package kz.ilotterytea.bot.builtin;

import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.api.permissions.Permissions;
import kz.ilotterytea.bot.models.ArgumentsModel;
import kz.ilotterytea.bot.utils.StringUtils;

import java.lang.management.ManagementFactory;

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
    public String run(ArgumentsModel m) {
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();

        String ut = StringUtils.formatTimestamp(uptime / 1000);

        return String.format(
                "%s: DankCrouching \u2615 Java %s \u00b7 Uptime: %s \u00b7 TMI: %sms",
                m.getEvent().getUser().getName(),
                System.getProperty("java.version"),
                ut,
                Huinyabot.getInstance().getClient().getChat().getLatency()
        );
    }
}
