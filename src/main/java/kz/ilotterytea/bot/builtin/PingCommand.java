package kz.ilotterytea.bot.builtin;

import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.api.commands.Command;
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
    public String run(IRCMessageEvent ev) {
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();

        String ut = StringUtils.formatTimestamp(uptime / 1000);

        return String.format(
                "%s: DankCrouching \u2615 Java %s \u00b7 Uptime: %s \u00b7 TMI: %sms",
                ev.getUser().getName(),
                System.getProperty("java.version"),
                ut,
                Huinyabot.getInstance().getClient().getChat().getLatency()
        );
    }
}
