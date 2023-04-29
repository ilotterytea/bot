package kz.ilotterytea.bot.models;

import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import kz.ilotterytea.bot.entities.permissions.UserPermission;
import kz.ilotterytea.bot.entities.users.User;

/**
 * Message arguments model.
 * @author ilotterytea
 * @since 1.0
 */
public class ArgumentsModel {
    private final User sender;
    private UserPermission currentPermissions;
    private String language;
    private final MessageModel msg;
    private final IRCMessageEvent event;

    public ArgumentsModel(
            User sender,
            String language,
            UserPermission permission,
            MessageModel msg,
            IRCMessageEvent event
    ) {
        this.sender = sender;
        this.language = language;
        this.currentPermissions = permission;
        this.msg = msg;
        this.event = event;
    }

    public User getSender() { return sender; }
    public UserPermission getCurrentPermissions() { return currentPermissions; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public MessageModel getMessage() { return msg; }
    public IRCMessageEvent getEvent() { return event; }
}
