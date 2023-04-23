package kz.ilotterytea.bot.thirdpartythings.seventv.eventapi.schemas.emoteset;

import java.util.List;

/**
 * @author ilotterytea
 * @since 1.0
 */
public class EmoteSetBody {
    private String id;
    private Integer kind;
    private EmoteSetUser actor;

    private List<EmoteSetBodyObject> pushed;
    private List<EmoteSetBodyObject> pulled;
    private List<EmoteSetBodyObject> updated;

    public EmoteSetBody() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getKind() {
        return kind;
    }

    public void setKind(Integer kind) {
        this.kind = kind;
    }

    public EmoteSetUser getActor() {
        return actor;
    }

    public void setActor(EmoteSetUser actor) {
        this.actor = actor;
    }

    public List<EmoteSetBodyObject> getPushed() {
        return pushed;
    }

    public void setPushed(List<EmoteSetBodyObject> pushed) {
        this.pushed = pushed;
    }

    public List<EmoteSetBodyObject> getPulled() {
        return pulled;
    }

    public void setPulled(List<EmoteSetBodyObject> pulled) {
        this.pulled = pulled;
    }

    public List<EmoteSetBodyObject> getUpdated() {
        return updated;
    }

    public void setUpdated(List<EmoteSetBodyObject> updated) {
        this.updated = updated;
    }
}
