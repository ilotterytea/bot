package kz.ilotterytea.bot.models.serverresponse.ivr;

/**
 * User info model for IVR API
 * @author ilotterytea
 * @since 1.5
 */
public class UserInfo {
    private Boolean banned;
    private String banReason;
    private String displayName;
    private String login;
    private String id;
    private String bio;
    private Integer follows;
    private Integer followers;
    private String chatColor;
    private String logo;
    private String banner;
    private String createdAt;
    private String updatedAt;

    public UserInfo() {}

    public Boolean getBanned() {
        return banned;
    }

    public String getBanReason() {
        return banReason;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLogin() {
        return login;
    }

    public String getId() {
        return id;
    }

    public String getBio() {
        return bio;
    }

    public Integer getFollows() {
        return follows;
    }

    public Integer getFollowers() {
        return followers;
    }

    public String getChatColor() {
        return chatColor;
    }

    public String getLogo() {
        return logo;
    }

    public String getBanner() {
        return banner;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
