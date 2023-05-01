package kz.ilotterytea.bot.entities.channels;

/**
 * Channel features.
 * @author ilotterytea
 * @version 1.4
 */
public enum ChannelFeature {
    NOTIFY_7TV("notify_7tv_events"),
    MARKOV_FEATURE("markov_feature");

    private String id;

    ChannelFeature(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
