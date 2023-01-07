package kz.ilotterytea.bot.models;

import com.github.twitch4j.chat.events.channel.IRCMessageEvent;

/**
 * Message arguments model.
 * @author ilotterytea
 * @since 1.0
 */
public class ArgumentsModel {
    private final UserModel sender;
    private final MessageModel msg;
    private final IRCMessageEvent event;

    public ArgumentsModel(
            UserModel sender,
            MessageModel msg,
            IRCMessageEvent event
    ) {
        this.sender = sender;
        this.msg = msg;
        this.event = event;
    }

    public UserModel getSender() { return sender; }
    public MessageModel getMessage() { return msg; }
    public IRCMessageEvent getEvent() { return event; }
}
