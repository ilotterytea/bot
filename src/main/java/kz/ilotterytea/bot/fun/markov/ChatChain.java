package kz.ilotterytea.bot.fun.markov;

/**
 * @author ilotterytea
 * @since 1.2
 */
public class ChatChain extends Chain {
    private ChainSender fromWordAuthor;
    private ChainSender toWordAuthor;

    public ChatChain(
            String fromWord,
            String toWord,
            ChainSender fromWordAuthor,
            ChainSender toWordAuthor
    ) {
        super(fromWord, toWord);
        this.fromWordAuthor = fromWordAuthor;
        this.toWordAuthor = toWordAuthor;
    }

    public ChainSender getFromWordAuthor() { return fromWordAuthor; }
    public ChainSender getToWordAuthor() { return toWordAuthor; }
    public void setFromWordAuthor(ChainSender author) { this.fromWordAuthor = author; }
    public void setToWordAuthor(ChainSender author) { this.toWordAuthor = author; }
}
