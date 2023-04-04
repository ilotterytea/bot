package kz.ilotterytea.bot.thirdpartythings.seventv.v3.schemas.wss;

import kz.ilotterytea.bot.thirdpartythings.seventv.v3.schemas.api.User;

import java.util.ArrayList;

public class BodyObject<T, Q, S> {
    private String id;
    private User actor;
    private ArrayList<T> pulled;
    private ArrayList<Q> pushed;
    private ArrayList<S> updated;

    public BodyObject() {}

    public String getId() {
        return id;
    }

    public User getActor() {
        return actor;
    }

    public ArrayList<T> getPulled() {
        return pulled;
    }

    public ArrayList<Q> getPushed() {
        return pushed;
    }

    public ArrayList<S> getUpdated() {
        return updated;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setActor(User actor) {
        this.actor = actor;
    }

    public void setPulled(ArrayList<T> pulled) {
        this.pulled = pulled;
    }

    public void setPushed(ArrayList<Q> pushed) {
        this.pushed = pushed;
    }

    public void setUpdated(ArrayList<S> updated) {
        this.updated = updated;
    }
}
