package kz.ilotterytea.bot.models;

import java.util.ArrayList;

/**
 * User (chatter) model.
 * @author ilotterytea
 * @since 1.0
 */
public class UserModel {
    private final String aliasId;
    private final ArrayList<String> flags;
    private String language;

    public UserModel(
            String aliasId,
            ArrayList<String> flags,
            String language
    ) {
        this.aliasId = aliasId;
        this.flags = flags;
        this.language = language;
    }

    public String getAliasId() { return aliasId; }
    public ArrayList<String> getFlags() { return flags; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
}
