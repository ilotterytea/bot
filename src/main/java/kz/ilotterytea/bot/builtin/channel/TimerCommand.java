package kz.ilotterytea.bot.builtin.channel;

import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.entities.Timer;
import kz.ilotterytea.bot.entities.channels.Channel;
import kz.ilotterytea.bot.entities.permissions.Permission;
import kz.ilotterytea.bot.entities.permissions.UserPermission;
import kz.ilotterytea.bot.entities.users.User;
import kz.ilotterytea.bot.i18n.LineIds;
import kz.ilotterytea.bot.utils.HibernateUtil;
import kz.ilotterytea.bot.utils.ParsedMessage;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Timer command.
 * @author ilotterytea
 * @since 1.5
 */
public class TimerCommand implements Command {
    @Override
    public String getNameId() { return "timer"; }

    @Override
    public int getDelay() { return 5000; }

    @Override
    public Permission getPermissions() { return Permission.BROADCASTER; }

    @Override
    public List<String> getOptions() { return Collections.emptyList(); }

    @Override
    public List<String> getSubcommands() { return List.of("new", "delete", "message", "interval", "list", "info"); }

    @Override
    public List<String> getAliases() { return Collections.emptyList(); }

    @Override
    public Optional<String> run(IRCMessageEvent event, ParsedMessage message, Channel channel, User user, UserPermission permission) {
        if (message.getSubcommandId().isEmpty()) {
            return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
                    channel.getPreferences().getLanguage(),
                    LineIds.NO_SUBCMD
            ));
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Timer> timers = session.createQuery("from Timer where channel = :channel", Timer.class)
                .setParameter("channel", channel)
                .getResultList();

        if (message.getSubcommandId().get().equals("list")) {
            session.close();
            return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                    channel.getPreferences().getLanguage(),
                    LineIds.C_TIMER_LIST,
                    channel.getAliasName(),
                    timers.stream().map(Timer::getName).collect(Collectors.joining(", "))
            ));
        }

        if (message.getMessage().isEmpty()) {
            session.close();
            return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
                    channel.getPreferences().getLanguage(),
                    LineIds.NO_MESSAGE
            ));
        }

        ArrayList<String> s = new ArrayList<>(List.of(message.getMessage().get().split(" ")));

        String timerId = s.get(0);
        s.remove(0);

        if (message.getSubcommandId().get().equals("new")) {
            if (timers.stream().anyMatch(t -> t.getName().equals(timerId))) {
                return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_TIMER_ALREADYEXISTS,
                        timerId
                ));
            }

            if (s.isEmpty() || s.size() < 2) {
                session.close();
                return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
                        channel.getPreferences().getLanguage(),
                        LineIds.NO_MESSAGE
                ));
            }

            int intervalMs;

            try {
                intervalMs = Integer.parseInt(s.get(0));
                s.remove(0);
            } catch (NumberFormatException e){
                return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_TIMER_NOTANINTERVAL,
                        s.get(0)
                ));
            }

            Timer timer = new Timer(channel, timerId, String.join(" ", s), intervalMs);
            channel.addTimer(timer);

            session.getTransaction().begin();
            session.persist(timer);
            session.merge(channel);
            session.getTransaction().commit();
            session.close();

            return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                    channel.getPreferences().getLanguage(),
                    LineIds.C_TIMER_NEW,
                    timerId
            ));
        }

        if (timers.stream().noneMatch(t -> t.getName().equals(timerId))) {
            session.close();
            return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                    channel.getPreferences().getLanguage(),
                    LineIds.C_TIMER_NOTEXISTS,
                    timerId
            ));
        }

        Timer timer = timers.stream().filter(t -> t.getName().equals(timerId)).findFirst().get();

        switch (message.getSubcommandId().get()) {
            case "delete":
                session.getTransaction().begin();
                session.remove(timer);
                session.getTransaction().commit();
                session.close();

                return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_TIMER_DELETE,
                        timerId
                ));
            case "info":
                session.close();
                return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_TIMER_INFO,
                        timerId,
                        String.valueOf(timer.getIntervalMilliseconds()),
                        timer.getMessage()
                ));
            default:
                break;
        }

        if (s.isEmpty()) {
            session.close();
            return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
                    channel.getPreferences().getLanguage(),
                    LineIds.NO_MESSAGE
            ));
        }

        String msg = String.join(" ", s);

        switch (message.getSubcommandId().get()) {
            case "message":
                timer.setMessage(msg);

                session.getTransaction().begin();
                session.persist(timer);
                session.getTransaction().commit();
                session.close();

                return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_TIMER_MESSAGE,
                        timerId
                ));
            case "interval":
                int interval;

                try {
                    interval = Integer.parseInt(msg);
                } catch (NumberFormatException e) {
                    session.close();
                    return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                            channel.getPreferences().getLanguage(),
                            LineIds.C_TIMER_NOTANINTERVAL,
                            msg
                    ));
                }

                timer.setIntervalMilliseconds(interval);

                session.getTransaction().begin();
                session.persist(timer);
                session.getTransaction().commit();
                session.close();

                return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_TIMER_INTERVAL,
                        timerId
                ));
            default:
                break;
        }

        return Optional.empty();
    }
}
