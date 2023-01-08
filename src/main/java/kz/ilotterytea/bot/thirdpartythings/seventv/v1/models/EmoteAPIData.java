package kz.ilotterytea.bot.thirdpartythings.seventv.v1.models;

import java.util.ArrayList;

/**
 * The emote data from API.
 * @author ilotterytea
 * @since 1.1
 */
public class EmoteAPIData extends EmoteData {
    private final String id;
    private final OwnerDataWithRole owner;
    private final ArrayList<String> visibility_simple;
    private final int status;

    public EmoteAPIData(
            String id,
            String name,
            OwnerDataWithRole owner,
            int visibility,
            ArrayList<String> visibility_simple,
            String mime,
            int status,
            ArrayList<String> tags,
            ArrayList<Integer> width,
            ArrayList<Integer> height,
            ArrayList<ArrayList<String>> urls
    ) {
        super(name, visibility, mime, tags, width, height, urls);
        this.id = id;
        this.owner = owner;
        this.visibility_simple = visibility_simple;
        this.status = status;
    }

    public String getId() { return id; }
    public OwnerDataWithRole getOwner() { return owner; }
    public ArrayList<String> getVisibilitySimple() { return visibility_simple; }
    public int getStatus() { return status; }
}
