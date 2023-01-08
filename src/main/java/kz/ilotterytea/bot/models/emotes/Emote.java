package kz.ilotterytea.bot.models.emotes;

/**
 * The emote.
 * @author ilotterytea
 * @since 1.1
 */
public class Emote {
    /** Emote ID from the provider. */
    private final String id;
    /** The emote provider. */
    private final Provider provider;
    /** The current name of the emote. */
    private String name;
    /** TA count of how many times the emote has been used. */
    private int count;
    /** Is this emote a global? */
    private boolean isGlobal;
    /** Has this emote been removed from the channel? */
    private boolean isDeleted;

    public Emote(
            String id,
            Provider provider,
            String name,
            int count,
            boolean isGlobal,
            boolean isDeleted
    ) {
        this.id = id;
        this.provider = provider;
        this.name = name;
        this.count = count;
        this.isGlobal = isGlobal;
        this.isDeleted = isDeleted;
    }

    public String getId() { return id; }
    public Provider getProvider() { return provider; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public boolean isGlobal() { return isGlobal; }
    public void setGlobal(boolean isGlobal) { this.isGlobal = isGlobal; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean isDeleted) { this.isDeleted = isDeleted; }
}
