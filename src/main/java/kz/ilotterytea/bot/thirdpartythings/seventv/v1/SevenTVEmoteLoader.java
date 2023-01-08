package kz.ilotterytea.bot.thirdpartythings.seventv.v1;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import kz.ilotterytea.bot.SharedConstants;
import kz.ilotterytea.bot.net.HttpFactory;
import kz.ilotterytea.bot.net.models.Response;
import kz.ilotterytea.bot.thirdpartythings.EmoteLoader;
import kz.ilotterytea.bot.thirdpartythings.seventv.v1.models.EmoteAPIData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * The 7TV emote loader.
 * @author ilotterytea
 * @since 1.1
 */
public class SevenTVEmoteLoader implements EmoteLoader<EmoteAPIData> {
    private final Logger LOGGER = LoggerFactory.getLogger(SevenTVEmoteLoader.class);

    @Override
    public ArrayList<EmoteAPIData> getChannelEmotes(String channelName) {
        Response response = HttpFactory.sendGETRequest(String.format(SharedConstants.STV_CHANNEL_EMOTES_URL, channelName));

        if (response == null || response.getResponse() == null || response.getCode() != 200) {
            LOGGER.debug("No response.");
            return null;
        }

        return new Gson().fromJson(response.getResponse(), new TypeToken<ArrayList<EmoteAPIData>>(){}.getType());
    }

    @Override
    public ArrayList<EmoteAPIData> getGlobalEmotes() {
        Response response = HttpFactory.sendGETRequest(SharedConstants.STV_GLOBAL_EMOTES_URL);

        if (response == null || response.getResponse() == null || response.getCode() != 200) {
            LOGGER.debug("No response.");
            return null;
        }

        return new Gson().fromJson(response.getResponse(), new TypeToken<ArrayList<EmoteAPIData>>(){}.getType());
    }
}
