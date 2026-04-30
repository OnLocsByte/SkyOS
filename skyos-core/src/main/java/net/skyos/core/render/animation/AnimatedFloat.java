package net.skyos.core.render.animation;

public final class AnimatedFloat {

    private float current;
    private float target;
    private float durationMs;
    private float elapsedMs;
    private float startValue;
    private Easing.Type easing;
    private boolean running;

    public AnimatedFloat(float initial) {
        this.current = initial;
        this.target = initial;
        this.easing = Easing.Type.EASE_OUT_QUAD;
        this.durationMs = 200f;
    }

    public AnimatedFloat easing(Easing.Type easing) {
        this.easing = easing;
        return this;
    }

    public AnimatedFloat duration(float ms) {
        this.durationMs = ms;
        return this;
    }

    public void animateTo(float target) {
        if (this.target == target && !running) return;
        this.startValue = current;
        this.target = target;
        this.elapsedMs = 0f;
        this.running = true;
    }

    public void set(float value) {
        this.current = value;
        this.target = value;
        this.running = false;
    }

    public void tick(float deltaMs) {
        if (!running) return;
        elapsedMs += deltaMs;
        float t = Math.min(1f, elapsedMs / durationMs);
        float easedT = Easing.apply(easing, t);
        current = startValue + (target - startValue) * easedT;
        if (t >= 1f) {
            current = target;
            running = false;
        }
    }

    public float get() { return current; }
    public float getTarget() { return target; }
    public boolean isRunning() { return running; }
    public boolean hasReached() { return !running && current == target; }
}
