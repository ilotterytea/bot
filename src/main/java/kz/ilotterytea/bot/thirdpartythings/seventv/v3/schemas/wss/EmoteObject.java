package kz.ilotterytea.bot.thirdpartythings.seventv.v3.schemas.wss;

public class EmoteObject {
    private String actor_id;
    private Integer flags;
    private String id;
    private String name;

    public EmoteObject() {}

    public String getActor_id() {
        return actor_id;
    }

    public Integer getFlags() {
        return flags;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setActor_id(String actor_id) {
        this.actor_id = actor_id;
    }

    public void setFlags(Integer flags) {
        this.flags = flags;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
