package kz.ilotterytea.bot.builtin.events;

import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.entities.channels.Channel;
import kz.ilotterytea.bot.entities.events.Event;
import kz.ilotterytea.bot.entities.events.EventFlag;
import kz.ilotterytea.bot.entities.events.EventType;
import kz.ilotterytea.bot.entities.events.subscriptions.EventSubscription;
import kz.ilotterytea.bot.entities.permissions.Permission;
import kz.ilotterytea.bot.entities.permissions.UserPermission;
import kz.ilotterytea.bot.entities.users.User;
import kz.ilotterytea.bot.i18n.LineIds;
import kz.ilotterytea.bot.utils.ParsedMessage;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Notify command.
 * @author ilotterytea
 * @since 1.6
 */
public class NotifyCommand implements Command {
    @Override
    public String getNameId() { return "notify"; }

    @Override
    public int getDelay() { return 5000; }

    @Override
    public Permission getPermissions() { return Permission.USER; }

    @Override
    public List<String> getOptions() { return Collections.emptyList(); }

    @Override
    public List<String> getSubcommands() { return List.of("sub", "unsub", "list", "subs"); }

    @Override
    public List<String> getAliases() { return Collections.singletonList("n"); }

    @Override
    public Optional<String> run(Session session, IRCMessageEvent event, ParsedMessage message, Channel channel, User user, UserPermission permission) {
        //
        if (message.getSubcommandId().isEmpty()) {
            return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
                    channel.getPreferences().getLanguage(),
                    LineIds.NO_SUBCMD
            ));
        }

        final String subcommandId = message.getSubcommandId().get();

        if (subcommandId.equals("list")) {
            List<Event> events = channel.getEvents().stream().filter(it -> !it.getFlags().contains(EventFlag.NON_SUBSCRIPTION)).collect(Collectors.toList());

            if (events.isEmpty()) {
                return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_NOTIFY_NOEVENTS
                ));
            }

            return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                    channel.getPreferences().getLanguage(),
                    LineIds.C_NOTIFY_LIST,
                    events.stream().map(it -> {
                        if (it.getEventType().equals(EventType.CUSTOM)) {
                            return "\"" + it.getEventName() + "\"";
                        }

                        return "\"" + it.getAliasId() + ":" + it.getEventType().getName() + "\"";
                    }).collect(Collectors.joining(","))
            ));
        }

        if (subcommandId.equals("subs")) {
            List<EventSubscription> eventSubscriptions = new ArrayList<>(user.getSubscriptions());

            if (eventSubscriptions.isEmpty()) {
                return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_NOTIFY_NOSUBS
                ));
            }

            return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                    channel.getPreferences().getLanguage(),
                    LineIds.C_NOTIFY_SUBS,
                    eventSubscriptions.stream().map(it -> {
                        if (it.getEvent().getEventType().equals(EventType.CUSTOM)) {
                            return "\"" + it.getEvent().getEventName() + "\"";
                        }

                        return "\"" + it.getEvent().getAliasId() + ":" + it.getEvent().getEventType().getName() + "\"";
                    }).collect(Collectors.joining(","))
            ));
        }

        // Clauses that requires a message
        if (message.getMessage().isEmpty()) {
            return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
                    channel.getPreferences().getLanguage(),
                    LineIds.NO_MESSAGE
            ));
        }

        final String msg = message.getMessage().get();
        ArrayList<String> msgSplit = new ArrayList<>(List.of(msg.split(" ")));

        final String formattedEventName;
        final String eventName;
        final EventType eventType;
        String[] targetAndEvent = msgSplit.get(0).split(":");

        if (targetAndEvent.length == 2) {
            Optional<EventType> optionalEventType = EventType.findEventTypeById(targetAndEvent[1]);

            if (optionalEventType.isEmpty()) {
                eventType = EventType.CUSTOM;
                eventName = msgSplit.get(0);
                formattedEventName = msgSplit.get(0) + " [CUSTOM]";
            } else {
                eventType = optionalEventType.get();
                formattedEventName = targetAndEvent[0] + ":" + targetAndEvent[1];

                try {
                    List<com.github.twitch4j.helix.domain.User> users = Huinyabot.getInstance().getClient().getHelix().getUsers(
                            Huinyabot.getInstance().getCredential().getAccessToken(),
                            null,
                            Collections.singletonList(targetAndEvent[0])
                    ).execute().getUsers();

                    if (users.isEmpty()) {
                        return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
                                channel.getPreferences().getLanguage(),
                                LineIds.NO_TWITCH_USER
                        ));
                    }

                    eventName = users.get(0).getId();
                } catch (Exception e) {
                    e.printStackTrace();

                    return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
                            channel.getPreferences().getLanguage(),
                            LineIds.SOMETHING_WENT_WRONG
                    ));
                }
            }
        } else {
            eventType = EventType.CUSTOM;
            eventName = msgSplit.get(0);
            formattedEventName = msgSplit.get(0);
        }

        msgSplit.remove(0);

        Optional<Event> optionalEvent = channel.getEvents()
                .stream()
                .filter(it -> {
                    if (eventType == EventType.CUSTOM) {
                        return it.getEventName().equals(eventName);
                    }

                    return it.getAliasId().equals(Integer.parseInt(eventName)) && it.getEventType().equals(eventType);
                })
                .findFirst();

        if (optionalEvent.isEmpty()) {
            return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                    channel.getPreferences().getLanguage(),
                    LineIds.C_NOTIFY_NOTEXISTS,
                    formattedEventName
            ));
        }

        Event event1 = optionalEvent.get();
        Optional<EventSubscription> optionalEventSubscription = user.getSubscriptions()
                .stream()
                .filter(it -> it.getEvent().getId().equals(event1.getId()))
                .findFirst();

        if (subcommandId.equals("sub")) {
            if (optionalEventSubscription.isPresent()) {
                return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_NOTIFY_SUBALREADY
                ));
            }

            if (event1.getFlags().contains(EventFlag.NON_SUBSCRIPTION)) {
                return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_NOTIFY_NOTAVAILABLE
                ));
            }

            EventSubscription eventSubscription = new EventSubscription();
            user.addSubscription(eventSubscription);
            event1.addSubscription(eventSubscription);

            session.persist(eventSubscription);
            session.merge(user);
            session.merge(event1);

            return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                    channel.getPreferences().getLanguage(),
                    LineIds.C_NOTIFY_SUB,
                    formattedEventName
            ));
        }

        if (subcommandId.equals("unsub")) {
            if (optionalEventSubscription.isEmpty()) {
                return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_NOTIFY_NOTSUBBED
                ));
            }

            session.remove(optionalEventSubscription.get());

            return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                    channel.getPreferences().getLanguage(),
                    LineIds.C_NOTIFY_UNSUB,
                    formattedEventName
            ));
        }

        return Optional.empty();
    }
}
