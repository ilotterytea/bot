package kz.ilotterytea.bot.fun.markov;

/**
 * The message chain.
 * @author ilotterytea
 * @since 1.2
 */
public class Chain {
    private final String from;
    private String to;

    public Chain(
            String fromWord,
            String toWord
    ) {
        this.from = fromWord;
        this.to = toWord;
    }

    public String getFromWord() { return from; }
    public String getToWord() { return to; }
    public void setToWord(String toWord) { this.to = toWord; }
}
