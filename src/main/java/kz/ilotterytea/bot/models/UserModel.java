package kz.ilotterytea.bot.models;

import kz.ilotterytea.bot.api.permissions.Permissions;

/**
 * User (chatter) model.
 * @author ilotterytea
 * @since 1.0
 */
public class UserModel {
    private final String aliasId;
    private Permissions permissions;

    public UserModel(
            String aliasId,
            Permissions permissions
    ) {
        this.aliasId = aliasId;
        this.permissions = permissions;
    }

    public String getAliasId() { return aliasId; }
    public Permissions getPermissions() { return permissions; }
    public void setPermissions(Permissions permissions) { this.permissions = permissions; }
}
