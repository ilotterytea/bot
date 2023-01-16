package kz.ilotterytea.bot.thirdpartythings.seventv.v1;

import com.google.gson.Gson;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.SharedConstants;
import kz.ilotterytea.bot.i18n.LineIds;
import kz.ilotterytea.bot.models.TargetModel;
import kz.ilotterytea.bot.models.emotes.Emote;
import kz.ilotterytea.bot.models.emotes.Provider;
import kz.ilotterytea.bot.thirdpartythings.seventv.v1.models.EmoteEventUpdate;
import kz.ilotterytea.bot.thirdpartythings.seventv.v1.models.Message;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * The websocket client for SevenTV connections.
 * @author ilotterytea
 * @since 1.1
 */
public class SevenTVWebsocketClient extends WebSocketClient {
    private final Logger LOGGER = LoggerFactory.getLogger(SevenTVWebsocketClient.class);

    public SevenTVWebsocketClient() throws URISyntaxException{
        super(new URI(SharedConstants.STV_EVENTAPI_ENDPOINT_URL));
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        LOGGER.debug(String.format(
                "Connected to the 7TV EventAPI! (%s -> %s %s)",
                SharedConstants.STV_EVENTAPI_ENDPOINT_URL,
                handshakedata.getHttpStatus(),
                handshakedata.getHttpStatusMessage()
        ));
    }

    @Override
    public void onMessage(String message) {
        Huinyabot bot = Huinyabot.getInstance();
        Message msg = new Gson().fromJson(message, Message.class);

        if (Objects.equals(msg.getAction(), "update")) {
            EmoteEventUpdate update = new Gson().fromJson(msg.getPayload(), EmoteEventUpdate.class);

            TargetModel target = bot.getTargetCtrl().get(
                    bot.getTargetLinks().get(update.getChannel())
            );

            if (!target.getEmotes().containsKey(Provider.SEVENTV)) {
                target.getEmotes().put(Provider.SEVENTV, new HashMap<>());
            }

            Map<String, Emote> emotes = target.getEmotes().get(Provider.SEVENTV);

            switch (update.getAction()) {
                case "ADD": {
                    if (!Huinyabot.getInstance().getTargetCtrl().getOrDefault(
                            Huinyabot.getInstance().getTargetLinks().get(update.getChannel())
                    ).getListeningMode()) {
                        bot.getClient().getChat().sendActionMessage(
                                update.getChannel(),
                                bot.getLocale().formattedText(
                                        target.getLanguage(),
                                        LineIds.NEW_EMOTE_WITH_AUTHOR,
                                        bot.getLocale().literalText(
                                                target.getLanguage(),
                                                LineIds.STV
                                        ),
                                        update.getActor(),
                                        update.getName()
                                )
                        );
                    }

                    if (emotes.containsKey(update.getEmoteId())) {
                        if (emotes.get(update.getEmoteId()).isDeleted()) {
                            emotes.get(update.getEmoteId()).setDeleted(false);
                        }
                        break;
                    }

                    emotes.put(
                            update.getEmoteId(),
                            new Emote(
                                    update.getEmoteId(),
                                    Provider.SEVENTV,
                                    update.getName(),
                                    0,
                                    false,
                                    false
                            )
                    );
                    break;
                }
                case "REMOVE": {
                    if (!Huinyabot.getInstance().getTargetCtrl().getOrDefault(
                            Huinyabot.getInstance().getTargetLinks().get(update.getChannel())
                    ).getListeningMode()) {
                        bot.getClient().getChat().sendActionMessage(
                                update.getChannel(),
                                bot.getLocale().formattedText(
                                        target.getLanguage(),
                                        LineIds.REMOVED_EMOTE_WITH_AUTHOR,
                                        bot.getLocale().literalText(
                                                target.getLanguage(),
                                                LineIds.STV
                                        ),
                                        update.getActor(),
                                        update.getName()
                                )
                        );
                    }
                    if (emotes.containsKey(update.getEmoteId())) {
                        emotes.get(update.getEmoteId()).setDeleted(true);
                    }
                    break;
                }
                case "UPDATE": {
                    if (!Huinyabot.getInstance().getTargetCtrl().getOrDefault(
                            Huinyabot.getInstance().getTargetLinks().get(update.getChannel())
                    ).getListeningMode()) {
                        bot.getClient().getChat().sendActionMessage(
                                update.getChannel(),
                                bot.getLocale().formattedText(
                                        target.getLanguage(),
                                        LineIds.UPDATED_EMOTE_WITH_AUTHOR,
                                        bot.getLocale().literalText(
                                                target.getLanguage(),
                                                LineIds.STV
                                        ),
                                        update.getActor(),
                                        (emotes.containsKey(update.getEmoteId())) ? emotes.get(update.getEmoteId()).getName() : update.getEmote().getName(),
                                        update.getName()
                                )
                        );
                    }

                    if (emotes.containsKey(update.getEmoteId())) {
                        emotes.get(update.getEmoteId()).setName(update.getName());
                    }
                    break;
                }
                default: break;
            }

            bot.getTargetCtrl().get(
                    bot.getTargetLinks().get(update.getChannel())
            ).setEmotes(Provider.SEVENTV, emotes);
        } else {
            LOGGER.debug(String.format("MESSAGE: Action: %s; Payload: %s", msg.getAction(), msg.getPayload()));
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        LOGGER.debug(String.format(
                "Connection to the 7TV EventAPI has been closed! Reason: %s %s (%s)",
                code,
                reason,
                (remote) ? "by the remote host" : "by the client"
        ));

        // Try to reconnect after 2 minutes:
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    SevenTVWebsocketClient.super.reconnectBlocking();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                if (SevenTVWebsocketClient.super.isOpen()) {
                    for (String username : Huinyabot.getInstance().getTargetLinks().keySet()) {
                        SevenTVWebsocketClient.super.send(
                                new Gson().toJson(new Message("join", username))
                        );
                    }

                    super.cancel();
                }
            }
        }, 120000, 120000);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }
}
