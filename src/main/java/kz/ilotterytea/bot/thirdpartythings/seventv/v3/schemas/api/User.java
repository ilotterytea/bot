package kz.ilotterytea.bot.thirdpartythings.seventv.v3.schemas.api;

public class User {
    private String id;
    private String platform;
    private String username;
    private String display_name;
    private STVUser user;

    public User(String id, String platform, String username, String display_name, STVUser user) {
        this.id = id;
        this.platform = platform;
        this.username = username;
        this.display_name = display_name;
        this.user = user;
    }

    public String getId() {
        return id;
    }

    public String getPlatform() {
        return platform;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public STVUser getUser() {
        return user;
    }
}
