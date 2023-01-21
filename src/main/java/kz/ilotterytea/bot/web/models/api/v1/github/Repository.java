package kz.ilotterytea.bot.web.models.api.v1.github;

/**
 * @author ilotterytea
 * @since 1.4
 */
public class Repository {
    private final long id;
    private final String name;
    private final String full_name;
    private final RepositorySender owner;

    public Repository(
            long id,
            String name,
            String full_name,
            RepositorySender owner
    ) {
        this.id = id;
        this.name = name;
        this.full_name = full_name;
        this.owner = owner;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return full_name;
    }

    public RepositorySender getOwner() {
        return owner;
    }
}
