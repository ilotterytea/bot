package kz.ilotterytea.bot.fun.markov;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Markov chain utilities.
 * @author ilotterytea
 * @since 1.2
 */
public class MarkovUtils {
    public static ArrayList<Chain> tokenizeText(String text) {
        ArrayList<Chain> list = new ArrayList<>();

        ArrayList<String> s = new ArrayList<>(Arrays.asList(text.split(" ")));
        String previousWord = "\\x02";

        for (String w : s) {
            list.add(new Chain(previousWord, w));
            previousWord = w;
        }

        list.add(new Chain(previousWord, "\\x03"));
        return list;
    }
}
