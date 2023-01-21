package kz.ilotterytea.bot.web.models.api.v1.github;

/**
 * @author ilotterytea
 * @since 1.4
 */
public class CommitAuthor {
    private final String name;

    public CommitAuthor(
            String name
    ) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
