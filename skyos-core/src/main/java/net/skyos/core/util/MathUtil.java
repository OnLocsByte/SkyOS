package net.skyos.core.util;

public final class MathUtil {

    private MathUtil() {}

    public static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    public static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * clamp(t, 0f, 1f);
    }

    public static double lerp(double a, double b, double t) {
        return a + (b - a) * Math.max(0.0, Math.min(1.0, t));
    }

    public static float smoothDamp(float current, float target, float smoothTime, float deltaTime) {
        float omega = 2f / smoothTime;
        float x = omega * deltaTime;
        float exp = 1f / (1f + x + 0.48f * x * x + 0.235f * x * x * x);
        float diff = current - target;
        float result = (current - target * (1f - exp)) + diff * exp;
        return target + result;
    }

    public static boolean inBounds(int px, int py, int x, int y, int w, int h) {
        return px >= x && px <= x + w && py >= y && py <= y + h;
    }

    public static int roundToGrid(int value, int gridSize) {
        return Math.round((float) value / gridSize) * gridSize;
    }

    public static float normalize(float value, float min, float max) {
        if (max == min) return 0f;
        return (value - min) / (max - min);
    }
}
