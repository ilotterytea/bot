package kz.ilotterytea.bot.builtin.events;

import com.github.twitch4j.helix.domain.Chatter;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.SharedConstants;
import kz.ilotterytea.bot.api.commands.*;
import kz.ilotterytea.bot.entities.channels.Channel;
import kz.ilotterytea.bot.entities.events.Event;
import kz.ilotterytea.bot.entities.events.EventFlag;
import kz.ilotterytea.bot.entities.events.EventType;
import kz.ilotterytea.bot.entities.permissions.Permission;
import kz.ilotterytea.bot.i18n.LineIds;
import kz.ilotterytea.bot.utils.ParsedMessage;
import kz.ilotterytea.bot.utils.StringUtils;
import org.hibernate.Session;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Event command.
 *
 * @author ilotterytea
 * @since 1.6
 */
public class EventCommand implements Command {
    @Override
    public String getNameId() {
        return "event";
    }

    @Override
    public int getDelay() {
        return 10000;
    }

    @Override
    public Permission getPermissions() {
        return Permission.MOD;
    }

    @Override
    public List<String> getSubcommands() {
        return List.of("on", "off", "list", "flag", "call");
    }

    @Override
    public List<String> getAliases() {
        return List.of("events", "ev");
    }

    @Override
    public Response run(Request request) throws Exception {
        ParsedMessage message = request.getMessage();
        Channel channel = request.getChannel();
        Session session = request.getSession();

        if (message.getSubcommandId().isEmpty()) {
            throw CommandException.notEnoughArguments(request, CommandArgument.SUBCOMMAND);
        }

        final String subcommandId = message.getSubcommandId().get();

        if (subcommandId.equals("list")) {
            Set<Event> events = channel.getEvents();
            List<com.github.twitch4j.helix.domain.User> users;

            try {
                users = Huinyabot.getInstance().getClient().getHelix().getUsers(
                        Huinyabot.getInstance().getCredential().getAccessToken(),
                        events.stream().filter(it -> it.getTargetAliasId() != null).map(it -> it.getTargetAliasId().toString()).collect(Collectors.toList()),
                        null
                ).execute().getUsers();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            if (events.isEmpty()) {
                return Response.ofSingle(Huinyabot.getInstance().getLocale().literalText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_EVENT_NOEVENTS
                ));
            } else {
                return Response.ofSingle(Huinyabot.getInstance().getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_EVENT_LIST,
                        channel.getAliasName(),
                        events.stream().map(it -> {
                            if (it.getEventType() == EventType.CUSTOM) {
                                return it.getCustomAliasId();
                            }

                            Optional<com.github.twitch4j.helix.domain.User> optionalUser = users
                                    .stream()
                                    .filter(u -> u.getId().equals(it.getTargetAliasId().toString()))
                                    .findFirst();

                            if (optionalUser.isEmpty()) {
                                return "";
                            }

                            String username = optionalUser.get().getLogin();

                            return username + ":" + it.getEventType().getName();
                        }).collect(Collectors.joining(", "))
                ));
            }
        }

        if (message.getMessage().isEmpty()) {
            throw CommandException.notEnoughArguments(request, CommandArgument.VALUE);
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
                formattedEventName = msgSplit.get(0);
            } else {
                eventType = optionalEventType.get();
                formattedEventName = targetAndEvent[0] + ":" + targetAndEvent[1];

                List<com.github.twitch4j.helix.domain.User> users = Huinyabot.getInstance().getClient().getHelix().getUsers(
                        Huinyabot.getInstance().getCredential().getAccessToken(),
                        null,
                        Collections.singletonList(targetAndEvent[0])
                ).execute().getUsers();

                if (users.isEmpty()) {
                    throw CommandException.notFound(request, targetAndEvent[0]);
                }

                eventName = users.get(0).getId();
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
                    if (it.getEventType() == EventType.CUSTOM && it.getTargetAliasId() == null) {
                        return it.getCustomAliasId().equals(eventName);
                    }

                    return it.getTargetAliasId().equals(Integer.parseInt(eventName)) && it.getEventType().equals(eventType);
                })
                .findFirst();

        if (subcommandId.equals("off")) {
            if (optionalEvent.isEmpty()) {
                throw CommandException.notFound(request, formattedEventName);
            }

            Event event1 = optionalEvent.get();

            if (!event1.getEventType().equals(EventType.CUSTOM)) {
                List<Event> events = session.createQuery("from Event where targetAliasId = :targetAliasId AND channel != :channel", Event.class)
                        .setParameter("targetAliasId", eventName)
                        .setParameter("channel", channel)
                        .getResultList();

                if (events.isEmpty()) {
                    Huinyabot.getInstance().getClient().getClientHelper().disableStreamEventListenerForId(eventName);
                }
            }

            session.remove(optionalEvent.get());

            return Response.ofSingle(Huinyabot.getInstance().getLocale().formattedText(
                    channel.getPreferences().getLanguage(),
                    LineIds.C_EVENT_OFF,
                    formattedEventName
            ));
        }

        if (subcommandId.equals("call")) {
            if (optionalEvent.isEmpty()) {
                throw CommandException.notFound(request, formattedEventName);
            }

            Event event1 = optionalEvent.get();
            final String ANNOUNCEMENT_LINE = Huinyabot.getInstance().getLocale().formattedText(
                    channel.getPreferences().getLanguage(),
                    LineIds.EVENTS_MESSAGE,
                    event1.getMessage()
            );

            Set<String> names = event1.getSubscriptions()
                    .stream()
                    .map(it -> it.getUser().getAliasName())
                    .collect(Collectors.toSet());

            if (event1.getFlags().contains(EventFlag.MASSPING)) {
                List<Chatter> chatters = Huinyabot.getInstance().getClient().getHelix().getChatters(
                        SharedConstants.TWITCH_TOKEN,
                        channel.getAliasId().toString(),
                        Huinyabot.getInstance().getCredential().getUserId(),
                        1000,
                        null
                ).execute().getChatters();

                names.addAll(chatters.stream().map(Chatter::getUserLogin).collect(Collectors.toSet()));
            }

            if (names.isEmpty()) {
                return Response.ofSingle(ANNOUNCEMENT_LINE);
            }

            List<String> formattedNames = StringUtils.joinStringsWithFixedLength(
                    ", ",
                    names.stream().map(it -> "@" + it).collect(Collectors.toList()),
                    500 - ANNOUNCEMENT_LINE.length()
            );

            return Response.ofMultiple(formattedNames.stream().map((formattedName) -> String.format("%s%s%s",
                    ANNOUNCEMENT_LINE,
                    Huinyabot.getInstance().getLocale().literalText(
                            channel.getPreferences().getLanguage(),
                            LineIds.EVENTS_MESSAGE_SUFFIX
                    ),
                    formattedName
            )).toList());
        }

        if (msgSplit.isEmpty()) {
            throw CommandException.notEnoughArguments(request, CommandArgument.MESSAGE);
        }

        final String finalMsg = String.join(" ", msgSplit);

        if (subcommandId.equals("on")) {
            if (optionalEvent.isPresent()) {
                Event event1 = optionalEvent.get();
                event1.setMessage(finalMsg);

                session.merge(event1);

                return Response.ofSingle(Huinyabot.getInstance().getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_EVENT_ONUPDATE,
                        formattedEventName
                ));
            }

            Event event1;

            if (eventType == EventType.CUSTOM) {
                event1 = new Event(eventName, eventType, finalMsg);
            } else {
                event1 = new Event(Integer.parseInt(eventName), eventType, finalMsg);

                Huinyabot.getInstance().getClient().getClientHelper().enableStreamEventListener(eventName, targetAndEvent[0]);
            }

            channel.addEvent(event1);

            session.persist(event1);
            session.merge(channel);

            if (eventType == EventType.CUSTOM) {
                return Response.ofSingle(Huinyabot.getInstance().getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_EVENT_ONCUSTOM,
                        eventName,
                        eventName,
                        eventName
                ));
            } else {
                return Response.ofSingle(Huinyabot.getInstance().getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_EVENT_ON,
                        eventType.getName(),
                        targetAndEvent[0]
                ));
            }
        }

        if (optionalEvent.isEmpty()) {
            throw CommandException.notFound(request, formattedEventName);
        }

        Event event1 = optionalEvent.get();

        if (subcommandId.equals("flag")) {
            Optional<EventFlag> optionalEventFlag = EventFlag.findEventFlagById(finalMsg);

            if (optionalEventFlag.isEmpty()) {
                throw CommandException.incorrectArgument(request, CommandArgument.FLAG, finalMsg);
            }

            EventFlag eventFlag = optionalEventFlag.get();

            if (event1.getFlags().contains(eventFlag)) {
                event1.removeFlag(eventFlag);

                session.merge(event1);

                return Response.ofSingle(Huinyabot.getInstance().getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_EVENT_UNFLAG,
                        eventFlag.getName(),
                        formattedEventName
                ));
            }

            event1.addFlag(eventFlag);

            session.merge(event1);

            return Response.ofSingle(Huinyabot.getInstance().getLocale().formattedText(
                    channel.getPreferences().getLanguage(),
                    LineIds.C_EVENT_FLAG,
                    eventFlag.getName(),
                    formattedEventName
            ));
        }

        return Response.ofNothing();
    }
}
