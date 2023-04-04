package kz.ilotterytea.bot.thirdpartythings.seventv.v3;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.SharedConstants;
import kz.ilotterytea.bot.i18n.LineIds;
import kz.ilotterytea.bot.models.emotes.Emote;
import kz.ilotterytea.bot.models.emotes.Provider;
import kz.ilotterytea.bot.thirdpartythings.seventv.v3.schemas.api.User;
import kz.ilotterytea.bot.thirdpartythings.seventv.v3.schemas.wss.*;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class SevenTVWebsocketClient extends WebSocketClient {
    private final Logger log = LoggerFactory.getLogger(SevenTVWebsocketClient.class.getSimpleName());

    private final OkHttpClient client;

    public SevenTVWebsocketClient() throws URISyntaxException {
        super(new URI(SharedConstants.STV_EVENTAPI_ENDPOINT_URL));
        this.client = new OkHttpClient.Builder().build();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        log.info(
                String.format(
                        "Connected to the 7TV EventAPI (V3)! %s %s - %s",
                    handshakedata.getHttpStatus(),
                    handshakedata.getHttpStatusMessage(),
                    SharedConstants.STV_EVENTAPI_ENDPOINT_URL
                )
        );
    }

    @Override
    public void onMessage(String message) {
        Huinyabot bot = Huinyabot.getInstance();
        Gson gson = new GsonBuilder().serializeNulls().create();

        Message<
                MessageData<
                        BodyObject<
                                PulledObject<EmoteObject>,
                                PushedObject<EmoteObject>,
                                UpdatedObject<EmoteObject>
                                >
                        >
                > msg = gson.fromJson(
                message,
                new TypeToken<Message<
                        MessageData<
                                BodyObject<
                                        PulledObject<EmoteObject>,
                                        PushedObject<EmoteObject>,
                                        UpdatedObject<EmoteObject>
                                        >
                                >
                        >>(){}.getType()
        );

        if (msg.getD().getType() == null) {
            log.debug("Received the 'ping' event... or some unnamed event... The processing will be skipped.");
            return;
        }

        log.debug("Received the '" + msg.getD().getType() + "' event!");

        // Get the target's 7TV:
        Request request = new Request.Builder()
                .get()
                .url("https://7tv.io/v3/emote-sets/" + msg.getD().getBody().getId())
                .build();

        String responseBody;

        try (Response response = client.newCall(request).execute()){
            if (response.body() == null) {
                log.debug("Response body of the '" + request.url() + "' request is null!");
                return;
            }

            responseBody = response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JsonObject userObject = gson.fromJson(responseBody, JsonObject.class);

        String targetName = userObject.getAsJsonObject("owner").get("username").getAsString();
        String targetId = bot.getTargetLinks().get(targetName);

        boolean isRemoved = msg.getD().getBody().getPulled() != null;
        boolean isUpdated = msg.getD().getBody().getUpdated() != null;

        String m = bot.getLocale().formattedText(
                bot.getTargetCtrl().get(targetId).getLanguage(),
                (isRemoved) ? LineIds.REMOVED_EMOTE_WITH_AUTHOR : (isUpdated) ? LineIds.UPDATED_EMOTE_WITH_AUTHOR : LineIds.NEW_EMOTE_WITH_AUTHOR,
                "7TV",
                msg.getD().getBody().getActor().getUsername(),
                (isUpdated) ? msg.getD().getBody().getUpdated().get(0).getOldValue().getName() :
                        (isRemoved) ? msg.getD().getBody().getPulled().get(0).getOldValue().getName() :
                                msg.getD().getBody().getPushed().get(0).getValue().getName(),
                (isUpdated) ? msg.getD().getBody().getUpdated().get(0).getValue().getName() : ""
        );

        Map<String, Emote> emoteMap = bot.getTargetCtrl().get(targetId).getEmotes().get(Provider.SEVENTV);

        if (isRemoved) {
            String id = msg.getD().getBody().getPulled().get(0).getOldValue().getId();
            Emote emote = emoteMap.get(id);

            emote.setDeleted(true);

            emoteMap.put(id, emote);
        } else if (isUpdated) {
            String id = msg.getD().getBody().getUpdated().get(0).getValue().getId();
            Emote emote = emoteMap.get(id);

            emote.setName(id);

            emoteMap.put(id, emote);
        } else {
            EmoteObject e = msg.getD().getBody().getPushed().get(0).getValue();
            Emote emote = new Emote(
                    e.getId(),
                    Provider.SEVENTV,
                    e.getName(),
                    0,
                    false,
                    false
            );

            emoteMap.put(e.getId(), emote);
        }

        bot.getTargetCtrl().get(targetId).setEmotes(Provider.SEVENTV, emoteMap);

        bot.getClient().getChat().sendMessage(
                targetName,
                m
        );
    }

    public void joinChannel(Integer twitchId) {
        Request request = new Request.Builder()
                .get()
                .url(String.format(SharedConstants.STV_USER_ENDPOINT_URL, twitchId.toString()))
                .build();

        Call call = client.newCall(request);
        User user;

        try (Response response = call.execute()){
            if (response.body() == null) {
                log.debug("Response body of the '" + request.url() + "' request is null!");
                return;
            }
            user = new Gson().fromJson(response.body().string(), User.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Map<String, String> condition = new HashMap<>();
        condition.put("object_id", user.getUser().getId());

        MessageData data = new MessageData("emote_set.update", condition, null);
        Message message = new Message(35, data);

        super.send(new Gson().toJson(message));
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        // TODO!!!
    }

    @Override
    public void onError(Exception ex) {
        // TODO!!!!!!!!!!!!!!!!!!!!!!!!!!!
    }
}
