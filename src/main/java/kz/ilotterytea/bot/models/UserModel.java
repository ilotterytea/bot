package kz.ilotterytea.bot.models;

/**
 * User (chatter) model.
 * @author ilotterytea
 * @since 1.0
 */
public class UserModel {
    private final String aliasId;
    private boolean isSuperuser;
    private boolean isSuspended;
    private String language;

    public UserModel(
            String aliasId,
            boolean isSuperuser,
            boolean isSuspended,
            String language
    ) {
        this.aliasId = aliasId;
        this.isSuperuser = isSuperuser;
        this.isSuspended = isSuspended;
        this.language = language;
    }

    public String getAliasId() { return aliasId; }
    public boolean isSuperUser() { return isSuperuser; }
    public void setSuperuser(boolean isSuperuser) { this.isSuperuser = isSuperuser; }
    public boolean isSuspended() { return isSuspended; }
    public void setSuspend(boolean isSuspended) { this.isSuspended = isSuspended; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
}
