package net.skyos.core.util;

import java.text.NumberFormat;
import java.util.Locale;

public final class StringUtil {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.US);

    private StringUtil() {}

    public static String stripFormatting(String text) {
        if (text == null) return null;
        return text.replaceAll("§[0-9a-fk-or]", "");
    }

    public static String formatNumber(long number) {
        if (number >= 1_000_000_000L) return String.format("%.1fB", number / 1_000_000_000.0);
        if (number >= 1_000_000L)     return String.format("%.1fM", number / 1_000_000.0);
        if (number >= 1_000L)         return String.format("%.1fK", number / 1_000.0);
        return String.valueOf(number);
    }

    public static String formatCommas(long number) {
        return NUMBER_FORMAT.format(number);
    }

    public static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 1) + "…";
    }

    public static String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return Character.toUpperCase(text.charAt(0)) + text.substring(1).toLowerCase(Locale.ROOT);
    }

    public static String toTitleCase(String text) {
        if (text == null || text.isEmpty()) return text;
        String[] words = text.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(capitalize(word));
        }
        return sb.toString();
    }

    public static String padLeft(String text, int width, char padChar) {
        if (text.length() >= width) return text;
        return String.valueOf(padChar).repeat(width - text.length()) + text;
    }

    public static boolean containsIgnoreFormatting(String text, String search) {
        return stripFormatting(text).toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT));
    }

    public static String formatTime(long seconds) {
        if (seconds < 60) return seconds + "s";
        if (seconds < 3600) return (seconds / 60) + "m " + (seconds % 60) + "s";
        return (seconds / 3600) + "h " + ((seconds % 3600) / 60) + "m";
    }
}
