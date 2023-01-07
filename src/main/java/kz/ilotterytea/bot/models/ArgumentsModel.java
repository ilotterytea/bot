package kz.ilotterytea.bot.models;

import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import kz.ilotterytea.bot.api.permissions.Permissions;

/**
 * Message arguments model.
 * @author ilotterytea
 * @since 1.0
 */
public class ArgumentsModel {
    private final UserModel sender;
    private Permissions currentPermissions;
    private final MessageModel msg;
    private final IRCMessageEvent event;

    public ArgumentsModel(
            UserModel sender,
            Permissions currentPermissions,
            MessageModel msg,
            IRCMessageEvent event
    ) {
        this.sender = sender;
        this.currentPermissions = currentPermissions;
        this.msg = msg;
        this.event = event;
    }

    public UserModel getSender() { return sender; }
    public Permissions getCurrentPermissions() { return currentPermissions; }
    public void setCurrentPermissions(Permissions currentPermissions) { this.currentPermissions = currentPermissions; }
    public MessageModel getMessage() { return msg; }
    public IRCMessageEvent getEvent() { return event; }
}
