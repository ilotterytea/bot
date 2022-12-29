package kz.ilotterytea.bot;

/**
 * @author ilotterytea
 * @since 1.0
 */
public class Main {
    public static void main(String[] args) {
        Huinyabot bot = new Huinyabot();

        Runtime.getRuntime().addShutdownHook(new Thread(bot::dispose));
        bot.init();
    }
}