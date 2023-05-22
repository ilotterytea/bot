package kz.ilotterytea.bot.handlers;

import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.events.ChannelGoOfflineEvent;
import com.github.twitch4j.helix.domain.Chatter;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.SharedConstants;
import kz.ilotterytea.bot.entities.channels.Channel;
import kz.ilotterytea.bot.entities.events.Event;
import kz.ilotterytea.bot.entities.events.EventFlag;
import kz.ilotterytea.bot.entities.events.EventType;
import kz.ilotterytea.bot.i18n.LineIds;
import kz.ilotterytea.bot.utils.HibernateUtil;
import kz.ilotterytea.bot.utils.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A collection of stream event handlers.
 * @author ilotterytea
 * @version 1.6
 */
public class StreamEventHandlers {
    private static final Huinyabot BOT = Huinyabot.getInstance();
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamEventHandlers.class.getName());

    /**
     * Handle a stream live event.
     * @param e event
     */
    public static void handleGoLiveEvent(ChannelGoLiveEvent e) {
        for (Event event : getEventsByAliasId(e.getChannel().getId(), EventType.LIVE)) {
            handleStreamEvent(event.getChannel(), event);
        }
    }

    /**
     * Handle a stream offline event.
     * @param e event
     */
    public static void handleGoOfflineEvent(ChannelGoOfflineEvent e) {
        for (Event event : getEventsByAliasId(e.getChannel().getId(), EventType.OFFLINE)) {
            handleStreamEvent(event.getChannel(), event);
        }
    }

    /**
     * Get listenables by alias ID and initialize their subscribers.
     * @param aliasId Alias ID
     */
    private static List<Event> getEventsByAliasId(String aliasId, EventType eventType) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Event> events = session.createQuery("from Events where aliasId = :aliasId AND eventType = :eventType", Event.class)
                .setParameter("aliasId", aliasId)
                .setParameter("eventType", eventType)
                .getResultList();

        for (Event event : events) {
            Hibernate.initialize(event.getSubscriptions());
        }

        session.close();

        return events;
    }

    /**
     * A base method for handling stream events.
     * @param channel Target channel.
     * @param event Event.
     */
    private static void handleStreamEvent(Channel channel, Event event) {
        final String ANNOUNCEMENT_LINE = BOT.getLocale().formattedText(
                channel.getPreferences().getLanguage(),
                LineIds.EVENTS_MESSAGE,
                event.getEventMessage()
        );

        final Set<String> names = event.getSubscriptions()
                .stream()
                .map(it -> it.getUser().getAliasName())
                .collect(Collectors.toSet());

        if (event.getFlags().contains(EventFlag.MASSPING)) {
            try {
                List<Chatter> chatters = BOT.getClient().getHelix()
                        .getChatters(
                                SharedConstants.TWITCH_ACCESS_TOKEN,
                                channel.getAliasId().toString(),
                                BOT.getCredential().getUserId(),
                                1000,
                                null
                        )
                        .execute()
                        .getChatters();

                names.addAll(chatters
                        .stream()
                        .map(Chatter::getUserLogin)
                        .collect(Collectors.toSet()));
            } catch (Exception e) {
                LOGGER.error("Can't get a list of chatters: " + e);
            }
        }

        if (names.isEmpty()) {
            BOT.getClient().getChat().sendMessage(
                    channel.getAliasName(),
                    ANNOUNCEMENT_LINE
            );
            return;
        }

        final List<String> formattedNameList = StringUtils.joinStringsWithFixedLength(
                ", ",
                names.stream().map(it -> "@" + it).collect(Collectors.toList()),
                500 - ANNOUNCEMENT_LINE.length()
        );

        for (String formattedName : formattedNameList) {
            BOT.getClient().getChat().sendMessage(
                    channel.getAliasName(),
                    String.format("%s%s%s",
                            ANNOUNCEMENT_LINE,
                            BOT.getLocale().literalText(
                                    channel.getPreferences().getLanguage(),
                                    LineIds.EVENTS_MESSAGE_SUFFIX
                            ),
                            formattedName
                    )
            );
        }
    }
}
