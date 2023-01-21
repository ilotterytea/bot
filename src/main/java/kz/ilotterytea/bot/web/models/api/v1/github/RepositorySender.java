package kz.ilotterytea.bot.web.models.api.v1.github;

/**
 * @author ilotterytea
 * @since 1.4
 */
public class RepositorySender {
    private final String login;
    private final String id;

    public RepositorySender(
            String login,
            String id
    ) {
        this.login = login;
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public String getId() {
        return id;
    }
}
