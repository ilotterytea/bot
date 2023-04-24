package kz.ilotterytea.bot.thirdpartythings.seventv.eventapi;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.SharedConstants;
import kz.ilotterytea.bot.entities.channels.Channel;
import kz.ilotterytea.bot.i18n.LineIds;
import kz.ilotterytea.bot.thirdpartythings.seventv.api.SevenTVAPIClient;
import kz.ilotterytea.bot.thirdpartythings.seventv.api.schemas.SevenTVUser;
import kz.ilotterytea.bot.thirdpartythings.seventv.api.schemas.User;
import kz.ilotterytea.bot.thirdpartythings.seventv.api.schemas.UserConnection;
import kz.ilotterytea.bot.thirdpartythings.seventv.api.schemas.emoteset.EmoteSet;
import kz.ilotterytea.bot.thirdpartythings.seventv.eventapi.schemas.*;
import kz.ilotterytea.bot.thirdpartythings.seventv.eventapi.schemas.emoteset.EmoteSetBody;
import kz.ilotterytea.bot.thirdpartythings.seventv.eventapi.schemas.emoteset.EmoteSetBodyObject;
import kz.ilotterytea.bot.utils.HibernateUtil;
import org.hibernate.Session;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * @author ilotterytea
 * @since 1.0
 */
public class SevenTVEventAPIClient extends WebSocketClient {
    private final Logger LOGGER = LoggerFactory.getLogger(SevenTVEventAPIClient.class.getName());

    private String sessionId;
    private boolean tryingToResume = false;

    private static SevenTVEventAPIClient instance;

    public static SevenTVEventAPIClient getInstance() {
        return instance;
    }

    public SevenTVEventAPIClient() throws URISyntaxException {
        super(new URI(SharedConstants.STV_EVENTAPI_ENDPOINT_URL));

        instance = this;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        LOGGER.info("Connected to 7TV EventAPI: " + handshakedata.getHttpStatus() + " " + handshakedata.getHttpStatusMessage());
    }

    @Override
    public void onMessage(String message) {
        Gson gson = new Gson();
        Payload payload = gson.fromJson(message, Payload.class);

        // Handling 'dispatch' events.
        // Here while the changes in the emote sets are being processed.
        if (payload.getOperation() == 0) {
            final Huinyabot BOT = Huinyabot.getInstance();

            Payload<PayloadData<EmoteSetBody>> emoteSetPayload = gson.fromJson(message, new TypeToken<Payload<PayloadData<EmoteSetBody>>>(){}.getType());
            EmoteSetBody body = emoteSetPayload.getData().getBody();

            // Getting information about the emote set:
            EmoteSet emoteSet = SevenTVAPIClient.getEmoteSet(body.getId());

            if (emoteSet == null) {
                LOGGER.debug("No emotesets for ID " + body.getId() + "! There will be no further processing!");
                return;
            }

            // Getting information about the 7tv user:
            SevenTVUser sevenTVUser = SevenTVAPIClient.getSevenTVUser(emoteSet.getOwner().getId());

            if (sevenTVUser == null || sevenTVUser.getConnections().isEmpty()) {
                LOGGER.debug("No SevenTV users for ID " + emoteSet.getOwner().getId() + " (or has no connections)! There will be no further processing!");
                return;
            }

            // Obtaining a Twitch connection only:
            Optional<UserConnection> connection = sevenTVUser.getConnections().stream().filter(p -> p.getPlatform().equals("TWITCH")).findFirst();

            if (connection.isEmpty()) {
                LOGGER.debug("No Twitch connections for SevenTV user ID " + sevenTVUser.getId() + "! There will be no further processing!");
                return;
            }

            // Obtaining the target model:
            Session session = HibernateUtil.getSessionFactory().openSession();
            List<Channel> channels = session.createQuery("from Channel where aliasId = :aliasId AND optOutTimestamp is null", Channel.class)
                    .setParameter("aliasId", connection.get().getId())
                    .getResultList();
            session.close();

            if (channels.isEmpty()) {
                LOGGER.debug("No channel for Twitch ID " + connection.get().getId() + "! There will be no further processing!");
                return;
            }

            Channel channel = channels.get(0);

            List<String> messages = new ArrayList<>();

            // Handling new emotes:
            if (body.getPushed() != null) {
                for (EmoteSetBodyObject object : body.getPushed()) {
                    messages.add(
                            BOT.getLocale().formattedText(
                                    channel.getPreferences().getLanguage(),
                                    LineIds.NEW_EMOTE_WITH_AUTHOR,
                                    BOT.getLocale().literalText(
                                            channel.getPreferences().getLanguage(),
                                            LineIds.STV
                                    ),
                                    body.getActor().getUsername(),
                                    object.getValue().getName()
                            )
                    );
                }
            }

            // Handling removed emotes:
            if (body.getPulled() != null) {
                System.out.println(body.getPulled());
                for (EmoteSetBodyObject object : body.getPulled()) {
                    messages.add(
                            BOT.getLocale().formattedText(
                                    channel.getPreferences().getLanguage(),
                                    LineIds.REMOVED_EMOTE_WITH_AUTHOR,
                                    BOT.getLocale().literalText(
                                            channel.getPreferences().getLanguage(),
                                            LineIds.STV
                                    ),
                                    body.getActor().getUsername(),
                                    object.getOldValue().getName()
                            )
                    );
                }
            }

            // Handling updated emotes:
            if (body.getUpdated() != null) {
                for (EmoteSetBodyObject object : body.getUpdated()) {
                    messages.add(
                            BOT.getLocale().formattedText(
                                    channel.getPreferences().getLanguage(),
                                    LineIds.UPDATED_EMOTE_WITH_AUTHOR,
                                    BOT.getLocale().literalText(
                                            channel.getPreferences().getLanguage(),
                                            LineIds.STV
                                    ),
                                    body.getActor().getUsername(),
                                    object.getOldValue().getName(),
                                    object.getValue().getName()
                            )
                    );
                }
            }

            // Sending the messages:
            for (String msg : messages) {
                BOT.getClient().getChat().sendMessage(
                        connection.get().getUsername(),
                        msg
                );
            }
        }

        // Handling 'hello' events.
        // This event is triggered when you connect to the server (I think).
        // Sending requests to subscribe changes of channel emotes from the database.
        else if (payload.getOperation() == 1) {
            handleHelloEvent(message);
        }

        // Handling 'heartbeat' events.
        else if (payload.getOperation() == 2) {
            LOGGER.debug("Received the heartbeat event!");
        }

        // Handling 'acknowledge' events.
        else if (payload.getOperation() == 5) {
            Payload<AcknowledgeData> ackPayload = gson.fromJson(message, new TypeToken<Payload<AcknowledgeData>>(){}.getType());

            String command = ackPayload.getData().getCommand();

            if (Objects.equals(command, "SUBSCRIBE")) {
                LOGGER.debug("Successfully subscribed!");
            } else if (Objects.equals(command, "RESUME") && tryingToResume) {
                Payload<AcknowledgeData<ResumeData>> acknowledgeDataPayload = gson.fromJson(message, new TypeToken<Payload<AcknowledgeData<ResumeData>>>(){}.getType());
                if (acknowledgeDataPayload.getData().getData().getSuccess()) {
                    LOGGER.debug("Successfully resumed the session!");
                    tryingToResume = false;
                } else {
                    LOGGER.debug("Can't resume the session! Maybe, session ID is invalid...");
                    tryingToResume = false;
                    sessionId = null;

                    handleHelloEvent(message);
                }
            }
        }

        // Handling other events.
        else {
            LOGGER.debug(String.format(
                    "Received the event (%s), but no handler found for it: %s",
                    payload.getOperation(),
                    message
            ));
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        LOGGER.debug("7TV closed the connection! Reason: " + code + " " + reason + " (Remote: " + remote + ").");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                LOGGER.debug("Trying to reconnect to 7TV...");
                try {
                    reconnectBlocking();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                if (isOpen()) {
                    LOGGER.debug("Successfully reconnected to 7TV!");
                    if (!resumeSession()) {
                        handleHelloEvent("{\"op\": 2, \"d\": null}");
                    }

                    super.cancel();
                } else {
                    LOGGER.debug("Couldn't reconnect to 7TV!");
                }
            }
        }, 300000, 300000);
    }

    @Override
    public void onError(Exception ex) {
        throw new RuntimeException(ex);
    }

    private boolean resumeSession() {
        if (sessionId == null) {
            LOGGER.debug("Can't resume because sessionId is null!");
            return false;
        }

        HashMap<String, String> map = new HashMap<>();
        map.put("session_id", sessionId);

        Payload<HashMap<String, String>> payload = new Payload<>(
                34,
                map
        );

        super.send(new Gson().toJson(payload));

        tryingToResume = true;
        return true;
    }

    public boolean joinChannel(Integer aliasId) {
        User stvUser = SevenTVAPIClient.getUser(aliasId);

        if (stvUser != null) {
            String json = new Gson().toJson(
                    new Payload<>(
                            35,
                            new PayloadData<>(
                                    "emote_set.update",
                                    new ConditionData(stvUser.getEmoteSet().getId())
                            )
                    )
            );

            super.send(json);
            return true;
        } else {
            return false;
        }
    }

    private void handleHelloEvent(String message) {
        Gson gson = new Gson();

        Payload payload = gson.fromJson(message, Payload.class);

        if (payload.getOperation() == 1) {
            Payload<HelloData> helloDataPayload = gson.fromJson(message, new TypeToken<Payload<HelloData>>(){}.getType());
            sessionId = helloDataPayload.getData().getSessionId();
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Channel> channels = session.createQuery("from Channel where optOutTimestamp is null", Channel.class)
                .getResultList();
        session.close();

        for (Channel channel : channels) {
            if (!joinChannel(channel.getAliasId())) {
                LOGGER.debug("Couldn't find the 7TV userdata for alias ID " + channel.getAliasId());
            }
        }
    }
}
