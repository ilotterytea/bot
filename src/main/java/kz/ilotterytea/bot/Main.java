package kz.ilotterytea.bot;

import io.micronaut.runtime.Micronaut;

/**
 * @author ilotterytea
 * @since 1.0
 */
public class Main {
    public static void main(String[] args) {
        Micronaut.build(args)
                .classes(Main.class)
                .start();

        Huinyabot bot = new Huinyabot();

        Runtime.getRuntime().addShutdownHook(new Thread(bot::dispose));
        bot.init();
    }
}