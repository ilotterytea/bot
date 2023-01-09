package kz.ilotterytea.bot.fun.markov;

/**
 * @author ilotterytea
 * @since 1.2
 */
public class ChainSender {
    private final String msgId;
    private final String channelId;
    private final String userId;

    public ChainSender(
            String msgId,
            String channelId,
            String userId
    ) {
        this.msgId = msgId;
        this.channelId = channelId;
        this.userId = userId;
    }

    public String getMsgId() { return msgId; }
    public String getChannelId() { return channelId; }
    public String getUserId() { return userId; }
}
