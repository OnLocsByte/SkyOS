package net.skyos.core.render.animation;

public final class Easing {

    private Easing() {}

    public static float linear(float t) {
        return t;
    }

    public static float easeInQuad(float t) {
        return t * t;
    }

    public static float easeOutQuad(float t) {
        return t * (2f - t);
    }

    public static float easeInOutQuad(float t) {
        return t < 0.5f ? 2f * t * t : -1f + (4f - 2f * t) * t;
    }

    public static float easeInCubic(float t) {
        return t * t * t;
    }

    public static float easeOutCubic(float t) {
        float t1 = t - 1f;
        return t1 * t1 * t1 + 1f;
    }

    public static float easeInOutCubic(float t) {
        return t < 0.5f ? 4f * t * t * t : (t - 1f) * (2f * t - 2f) * (2f * t - 2f) + 1f;
    }

    public static float easeOutBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1f;
        float t1 = t - 1f;
        return 1f + c3 * t1 * t1 * t1 + c1 * t1 * t1;
    }

    public static float easeOutElastic(float t) {
        if (t == 0f || t == 1f) return t;
        float c4 = (2f * (float) Math.PI) / 3f;
        return (float) (Math.pow(2, -10 * t) * Math.sin((t * 10f - 0.75f) * c4) + 1f);
    }

    public static float easeOutExpo(float t) {
        return t == 1f ? 1f : 1f - (float) Math.pow(2, -10 * t);
    }

    public static float easeInExpo(float t) {
        return t == 0f ? 0f : (float) Math.pow(2, 10 * t - 10);
    }

    public static float apply(Type type, float t) {
        t = Math.max(0f, Math.min(1f, t));
        return switch (type) {
            case LINEAR           -> linear(t);
            case EASE_IN_QUAD     -> easeInQuad(t);
            case EASE_OUT_QUAD    -> easeOutQuad(t);
            case EASE_IN_OUT_QUAD -> easeInOutQuad(t);
            case EASE_OUT_CUBIC   -> easeOutCubic(t);
            case EASE_IN_OUT_CUBIC-> easeInOutCubic(t);
            case EASE_OUT_BACK    -> easeOutBack(t);
            case EASE_OUT_ELASTIC -> easeOutElastic(t);
            case EASE_OUT_EXPO    -> easeOutExpo(t);
            case EASE_IN_EXPO     -> easeInExpo(t);
        };
    }

    public enum Type {
        LINEAR,
        EASE_IN_QUAD,
        EASE_OUT_QUAD,
        EASE_IN_OUT_QUAD,
        EASE_OUT_CUBIC,
        EASE_IN_OUT_CUBIC,
        EASE_OUT_BACK,
        EASE_OUT_ELASTIC,
        EASE_OUT_EXPO,
        EASE_IN_EXPO
    }
}
