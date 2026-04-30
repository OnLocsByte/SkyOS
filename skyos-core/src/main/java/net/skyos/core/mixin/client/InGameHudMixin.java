package net.skyos.core.mixin.client;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.skyos.core.SkyOSCoreClient;
import net.skyos.core.hypixel.PlayerState;
import net.skyos.core.event.SkyOSEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Inject(
        method = "setOverlayMessage(Lnet/minecraft/text/Text;Z)V",
        at = @At("HEAD")
    )
    private void onActionBar(Text message, boolean tinted, CallbackInfo ci) {
        if (message == null) return;
        String text = message.getString();
        SkyOSCoreClient instance = SkyOSCoreClient.getInstance();
        if (instance == null) return;

        instance.getEventBus().post(SkyOSEvents.ACTION_BAR, text);

        PlayerState playerState = PlayerState.getInstance();
        if (playerState != null && instance.getHypixelDetector().isOnSkyBlock()) {
            playerState.parseFromActionBar(text);
        }
    }
}
