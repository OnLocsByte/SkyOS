package net.skyos.core.mixin.network;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.skyos.core.SkyOSCoreClient;
import net.skyos.core.event.SkyOSEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    @Inject(
        method = "onGameMessage",
        at = @At("HEAD")
    )
    private void onChatMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        SkyOSCoreClient instance = SkyOSCoreClient.getInstance();
        if (instance == null) return;

        String message = packet.content().getString();
        instance.getEventBus().post(SkyOSEvents.CHAT_MESSAGE, message);
    }

    @Inject(
        method = "onGameJoin",
        at = @At("TAIL")
    )
    private void onJoinServer(CallbackInfo ci) {
        SkyOSCoreClient instance = SkyOSCoreClient.getInstance();
        if (instance == null) return;
        instance.getEventBus().post(SkyOSEvents.JOIN_SERVER, (ClientPlayNetworkHandler)(Object)this);
        instance.getHypixelDetector().onServerJoin((ClientPlayNetworkHandler)(Object)this);
    }
}
