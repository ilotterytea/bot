package kz.ilotterytea.bot.thirdpartythings.seventv.v1.models;

import java.util.ArrayList;

/**
 * The extra data of the emote.
 * @author ilotterytea
 * @since 1.1
 */
public class ExtraEmoteData {
    /** Original name of the emote. */
    private final String name;
    /** The visibility bitfield of this emote. */
    private final int visibility;
    /** The MIME type of the images. */
    private final String mime;
    /** The TAGs on this emote. */
    private final ArrayList<String> tags;
    /** The widths of the images. */
    private final ArrayList<Integer> width;
    /** The heights of the images. */
    private final ArrayList<Integer> height;
    /** The animation status of the emote. */
    private final boolean animated;
    /** Infomation about the uploader. */
    private final OwnerData owner;
    /** The first string in the inner array will contain the "name" of the URL, like "1" or "2" or "3" or "4"
      * or some custom event names we haven't figured out yet such as "christmas_1" or "halloween_1" for special versions of emotes.
      * The second string in the inner array will contain the actual CDN URL of the emote. You should use these URLs and not derive URLs
      * based on the emote ID and size you want.
      */
    private final ArrayList<ArrayList<String>> urls;

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
        this.name = name;
        this.visibility = visibility;
        this.mime = mime;
        this.tags = tags;
        this.width = width;
        this.height = height;
        this.animated = animated;
        this.owner = owner;
        this.urls = urls;
    }

    public String getName() { return name; }
    public int getVisibility() { return visibility; }
    public String getMime() { return mime; }
    public ArrayList<String> getTags() { return tags; }
    public ArrayList<Integer> getWidth() { return width; }
    public ArrayList<Integer> getHeight() { return height; }
    public boolean getAnimated() { return animated; }
    public OwnerData getOwner() { return owner; }
    public ArrayList<ArrayList<String>> getUrls() { return urls; }
}
