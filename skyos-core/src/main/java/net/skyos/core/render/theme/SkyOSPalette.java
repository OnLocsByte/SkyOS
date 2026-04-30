package net.skyos.core.render.theme;

/**
 * SkyOS visual identity color palette.
 * All colors are ARGB format (0xAARRGGBB).
 */
public final class SkyOSPalette {

    // Backgrounds
    public static final int BG_PRIMARY        = 0xE50A0E1A;
    public static final int BG_SECONDARY      = 0xE5121829;
    public static final int BG_ELEVATED       = 0xE51A2035;
    public static final int BG_CARD           = 0xCC0D1120;
    public static final int BG_OVERLAY        = 0xB0080C15;

    // Accent — blue
    public static final int ACCENT_PRIMARY    = 0xFF4A9EFF;
    public static final int ACCENT_SECONDARY  = 0xFF7B4FFF;
    public static final int ACCENT_GLOW       = 0x554A9EFF;
    public static final int ACCENT_GLOW_SOFT  = 0x224A9EFF;

    // Text
    public static final int TEXT_PRIMARY      = 0xFFEEF2FF;
    public static final int TEXT_SECONDARY    = 0xFFAAB4C8;
    public static final int TEXT_MUTED        = 0xFF6B7A9A;
    public static final int TEXT_ACCENT       = 0xFF4A9EFF;

    // Borders
    public static final int BORDER_SUBTLE     = 0x334A9EFF;
    public static final int BORDER_ACTIVE     = 0x994A9EFF;
    public static final int BORDER_GLOW       = 0xFF4A9EFF;

    // Status
    public static final int STATUS_SUCCESS    = 0xFF22C55E;
    public static final int STATUS_WARNING    = 0xFFF59E0B;
    public static final int STATUS_ERROR      = 0xFFEF4444;
    public static final int STATUS_INFO       = 0xFF4A9EFF;

    // Gradient endpoints (for gradient rendering)
    public static final int GRADIENT_TOP      = 0xFF1A2545;
    public static final int GRADIENT_BOTTOM   = 0xFF0A0E1A;

    // Transparency levels
    public static final int ALPHA_FULL        = 0xFF000000;
    public static final int ALPHA_HIGH        = 0xDD000000;
    public static final int ALPHA_MED         = 0xAA000000;
    public static final int ALPHA_LOW         = 0x66000000;
    public static final int ALPHA_SUBTLE      = 0x33000000;

    private SkyOSPalette() {}

    /** Blends color with given alpha (0-255), preserving RGB. */
    public static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    /** Linearly interpolates between two ARGB colors by factor t [0,1]. */
    public static int lerp(int colorA, int colorB, float t) {
        int aA = (colorA >> 24) & 0xFF, aB = (colorB >> 24) & 0xFF;
        int rA = (colorA >> 16) & 0xFF, rB = (colorB >> 16) & 0xFF;
        int gA = (colorA >> 8) & 0xFF,  gB = (colorB >> 8) & 0xFF;
        int bA = colorA & 0xFF,          bB = colorB & 0xFF;
        int a = (int) (aA + (aB - aA) * t);
        int r = (int) (rA + (rB - rA) * t);
        int g = (int) (gA + (gB - gA) * t);
        int b = (int) (bA + (bB - bA) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
