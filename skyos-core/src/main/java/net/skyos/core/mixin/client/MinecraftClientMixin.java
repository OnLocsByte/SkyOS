package net.skyos.core.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.skyos.core.SkyOSCoreClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Inject(method = "disconnect()V", at = @At("HEAD"))
    private void onDisconnect(CallbackInfo ci) {
        SkyOSCoreClient instance = SkyOSCoreClient.getInstance();
        if (instance == null) return;
        instance.getHypixelDetector().onServerLeave();
    }
}
