package net.skyos.core.mixin.client;

import net.minecraft.client.render.GameRenderer;
import net.skyos.core.SkyOSCoreClient;
import net.skyos.core.render.animation.AnimationEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(
        method = "render",
        at = @At("HEAD")
    )
    private void onRenderHead(CallbackInfo ci) {
        // Tick animation engine before every frame so animations are frame-rate aware
        SkyOSCoreClient instance = SkyOSCoreClient.getInstance();
        if (instance == null) return;
        AnimationEngine engine = instance.getRenderEngine().getAnimationEngine();
        if (engine != null) {
            engine.tick();
        }
    }
}
