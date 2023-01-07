package kz.ilotterytea.bot.thirdpartythings.seventv.v1;

import com.google.gson.Gson;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.SharedConstants;
import kz.ilotterytea.bot.thirdpartythings.seventv.v1.models.EmoteEventUpdate;
import kz.ilotterytea.bot.thirdpartythings.seventv.v1.models.Message;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

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
        Message msg = new Gson().fromJson(message, Message.class);

        if (Objects.equals(msg.getAction(), "update")) {
            EmoteEventUpdate update = new Gson().fromJson(msg.getPayload(), EmoteEventUpdate.class);

            switch (update.getAction()) {
                case "ADD": {
                    Huinyabot.getInstance().getClient().getChat().sendActionMessage(
                            update.getChannel(),
                            String.format(
                                    "[7TV] %s added the %s emote!",
                                    update.getActor(),
                                    update.getName()
                            )
                    );
                    break;
                }
                case "REMOVE": {
                    Huinyabot.getInstance().getClient().getChat().sendActionMessage(
                            update.getChannel(),
                            String.format(
                                    "[7TV] %s removed the %s emote!",
                                    update.getActor(),
                                    update.getName()
                            )
                    );
                    break;
                }
                case "UPDATE": {
                    Huinyabot.getInstance().getClient().getChat().sendActionMessage(
                            update.getChannel(),
                            String.format(
                                    "[7TV] %s changed the emote name from %s to %s !",
                                    update.getActor(),
                                    update.getEmote().getName(),
                                    update.getName()
                            )
                    );
                    break;
                }
                default: break;
            }
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

        // Try to reconnect once after 2 minutes:
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    SevenTVWebsocketClient.super.reconnectBlocking();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 120000);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }
}
