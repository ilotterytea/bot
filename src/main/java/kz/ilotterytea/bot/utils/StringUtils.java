package kz.ilotterytea.bot.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * String utilities.
 * @author ilotterytea
 * @since 1.0
 */
public class StringUtils {
    /**
     * Pad.
     * @param i The number.
     * @return if the number < 10, it returns "0" + number, otherwise just the number.
     */
    public static String pad(long i) { return (i > 10) ? String.valueOf(i) : "0" + i; }

    /**
     * Format long timestamp to humanized timestamp.
     * @param timestamp long timestamp.
     * @return humanized timestamp.
     */
    public static String formatTimestamp(long timestamp) {
        long d = Math.round(timestamp / (60 * 60 * 24));
        long h = Math.round(timestamp / (60 * 60) % 24);
        long m = Math.round(timestamp % (60 * 60) / 60);
        long s = Math.round(timestamp % 60);

        // Only seconds:
        if (d == 0 && h == 0 && m == 0) {
            return String.format("%ss", s);
        }
        // Minutes and seconds:
        else if (d == 0 && h == 0) {
            return String.format("%sm%ss", m, s);
        }
        // Hours and minutes:
        else if (d == 0) {
            return String.format("%sh%sm", h, m);
        }
        // Days and hours:
        else {
            return String.format("%sd%sh", d, h);
        }
    }

    /**
     * <p>
     *     Connect the lines with fixed length.
     *     If the string exceeds the specified length, the string will be added to the next element.
     * </p>
     *
     * <p>
     *     For example:
     *     <ul>
     *         <li>Input: ["hello", "my", "name", "is", "jeff"]</li>
     *         <li>Output: ["hello my", "name is", "jeff"]</li>
     *     </ul>
     * </p>
     * For example:
     * @param strings Strings
     * @param fixedLength Fixed length
     * @return a list of strings
     */
    public static List<String> joinStringsWithFixedLength(String separator, List<String> strings, int fixedLength) {
        List<String> outputStrings = new ArrayList<>();
        List<String> outputBuffer = new ArrayList<>();

        for (String string : strings) {
            String joinedBuffer = String.join(separator, outputBuffer);

            if (joinedBuffer.length() + separator.length() + string.length() > fixedLength) {
                outputStrings.add(joinedBuffer);
                outputBuffer.clear();
            }

            outputBuffer.add(string);
        }

        outputStrings.add(String.join(separator, outputBuffer));

        return outputStrings;
    }
}
