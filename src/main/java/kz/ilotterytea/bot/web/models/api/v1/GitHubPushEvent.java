package kz.ilotterytea.bot.web.models.api.v1;

import kz.ilotterytea.bot.web.models.api.v1.github.Commit;
import kz.ilotterytea.bot.web.models.api.v1.github.Repository;
import kz.ilotterytea.bot.web.models.api.v1.github.RepositorySender;

import java.util.ArrayList;

/**
 * @author ilotterytea
 * @since 1.4
 */
public class GitHubPushEvent {
    private final String before;
    private final String after;
    private final Repository repository;
    private final RepositorySender sender;
    private final ArrayList<Commit> commits;

    public GitHubPushEvent(
            String before,
            String after,
            Repository repository,
            RepositorySender sender,
            ArrayList<Commit> commits
    ) {
        this.before = before;
        this.after = after;
        this.repository = repository;
        this.sender = sender;
        this.commits = commits;
    }

    public String getBefore() {
        return before;
    }

    public String getAfter() {
        return after;
    }

    public Repository getRepository() {
        return repository;
    }

    public RepositorySender getSender() {
        return sender;
    }

    public ArrayList<Commit> getCommits() {
        return commits;
    }
}
