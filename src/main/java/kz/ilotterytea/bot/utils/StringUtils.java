package kz.ilotterytea.bot.utils;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

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

    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();
    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    public static String formatNumber(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return formatNumber(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + formatNumber(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }
}
