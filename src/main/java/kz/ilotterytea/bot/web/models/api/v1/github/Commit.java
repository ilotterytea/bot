package kz.ilotterytea.bot.web.models.api.v1.github;

import java.util.ArrayList;

/**
 * @author ilotterytea
 * @since 1.4
 */
public class Commit {
    private final String id;
    private final String message;
    private final String timestamp;
    private final CommitAuthor author;
    private final ArrayList<String> added;
    private final ArrayList<String> removed;
    private final ArrayList<String> modified;

    public Commit(
            String id,
            String message,
            String timestamp,
            CommitAuthor author,
            ArrayList<String> added,
            ArrayList<String> removed,
            ArrayList<String> modified
    ) {
        this.id = id;
        this.message = message;
        this.timestamp = timestamp;
        this.author = author;
        this.added = added;
        this.removed = removed;
        this.modified = modified;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public CommitAuthor getAuthor() {
        return author;
    }

    public ArrayList<String> getAdded() {
        return added;
    }

    public ArrayList<String> getRemoved() {
        return removed;
    }

    public ArrayList<String> getModified() {
        return modified;
    }
}
