package kz.ilotterytea.bot.thirdpartythings.seventv.v1.models;

import java.util.ArrayList;

/**
 * The extra data of the emote.
 * @author ilotterytea
 * @since 1.1
 */
public class ExtraEmoteData extends EmoteData {
    /** The animation status of the emote. */
    private final boolean animated;
    /** Infomation about the uploader. */
    private final OwnerData owner;

    public ExtraEmoteData(
            String name,
            int visibility,
            String mime,
            ArrayList<String> tags,
            ArrayList<Integer> width,
            ArrayList<Integer> height,
            boolean animated,
            OwnerData owner,
            ArrayList<ArrayList<String>> urls
    ) {
        super(name, visibility, mime, tags, width, height, urls);
        this.animated = animated;
        this.owner = owner;
    }

    public boolean getAnimated() { return animated; }
    public OwnerData getOwner() { return owner; }
}
