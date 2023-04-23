package kz.ilotterytea.bot.thirdpartythings.seventv.api.schemas.emoteset;

import kz.ilotterytea.bot.thirdpartythings.seventv.api.schemas.SevenTVUser;

import java.util.ArrayList;

/**
 * @author ilotterytea
 * @since 1.0
 */
public class EmoteSet {
    private String id;
    private String name;
    private ArrayList<Emote> emotes;
    private SevenTVUser owner;

    public EmoteSet() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Emote> getEmotes() {
        return emotes;
    }

    public void setEmotes(ArrayList<Emote> emotes) {
        this.emotes = emotes;
    }

    public SevenTVUser getOwner() {
        return owner;
    }

    public void setOwner(SevenTVUser owner) {
        this.owner = owner;
    }
}

