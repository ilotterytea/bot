package kz.ilotterytea.bot.thirdpartythings;

import java.util.ArrayList;

/**
 * The emote loader.
 * @author ilotterytea
 * @since 1.1
 */
public interface EmoteLoader<T> {
    /** Get the channel emotes. */
    ArrayList<T> getChannelEmotes(String channelName);
    /** Get the provider's global emotes. */
    ArrayList<T> getGlobalEmotes();
}
