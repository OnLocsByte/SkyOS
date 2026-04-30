package net.skyos.core.render.animation;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class AnimationEngine {

    private final Set<AnimatedFloat> tracked = ConcurrentHashMap.newKeySet();
    private long lastTickMs = System.currentTimeMillis();

    public AnimatedFloat track(AnimatedFloat anim) {
        tracked.add(anim);
        return anim;
    }

    public void untrack(AnimatedFloat anim) {
        tracked.remove(anim);
    }

    public void tick() {
        long now = System.currentTimeMillis();
        float delta = now - lastTickMs;
        lastTickMs = now;
        for (AnimatedFloat anim : tracked) {
            anim.tick(delta);
        }
    }

    public AnimatedFloat create(float initial) {
        AnimatedFloat anim = new AnimatedFloat(initial);
        track(anim);
        return anim;
    }

    public AnimatedFloat create(float initial, Easing.Type easing, float durationMs) {
        AnimatedFloat anim = new AnimatedFloat(initial)
                .easing(easing)
                .duration(durationMs);
        track(anim);
        return anim;
    }
}
