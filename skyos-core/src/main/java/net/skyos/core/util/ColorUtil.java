package net.skyos.core.util;

public final class ColorUtil {

    private ColorUtil() {}

    public static int argb(int a, int r, int g, int b) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public static int rgb(int r, int g, int b) {
        return argb(255, r, g, b);
    }

    public static int getAlpha(int color) { return (color >> 24) & 0xFF; }
    public static int getRed(int color)   { return (color >> 16) & 0xFF; }
    public static int getGreen(int color) { return (color >> 8)  & 0xFF; }
    public static int getBlue(int color)  { return color & 0xFF; }

    public static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    public static int multiplyAlpha(int color, float factor) {
        int alpha = (int) (getAlpha(color) * factor);
        return withAlpha(color, Math.max(0, Math.min(255, alpha)));
    }

    public static int lerp(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int aA = getAlpha(a), bA = getAlpha(b);
        int aR = getRed(a),   bR = getRed(b);
        int aG = getGreen(a), bG = getGreen(b);
        int aB = getBlue(a),  bB = getBlue(b);
        return argb(
            (int)(aA + (bA - aA) * t),
            (int)(aR + (bR - aR) * t),
            (int)(aG + (bG - aG) * t),
            (int)(aB + (bB - aB) * t)
        );
    }

    public static int fromHex(String hex) {
        String clean = hex.startsWith("#") ? hex.substring(1) : hex;
        if (clean.length() == 6) clean = "FF" + clean;
        return (int) Long.parseLong(clean, 16);
    }

    public static String toHex(int color) {
        return String.format("#%08X", color);
    }

    /** Converts HSB (hue 0-360, saturation 0-1, brightness 0-1) to ARGB. */
    public static int fromHSB(float hue, float saturation, float brightness) {
        int rgb = java.awt.Color.HSBtoRGB(hue / 360f, saturation, brightness);
        return 0xFF000000 | (rgb & 0x00FFFFFF);
    }

    /** Returns a rainbow color cycling based on time. */
    public static int rainbow(long timeMs, float saturation, float brightness) {
        float hue = (timeMs % 6000L) / 6000f * 360f;
        return fromHSB(hue, saturation, brightness);
    }
}
