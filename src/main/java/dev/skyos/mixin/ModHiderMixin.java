package dev.skyos.mixin;

import dev.skyos.features.general.ModHider;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts outgoing packets to suppress channel registration payloads
 * for mods that are configured to be hidden from the server.
 *
 * The Fabric API registers mod channels by sending a ServerboundCustomPayloadPacket
 * with payload ID "minecraft:register". When Mod Hider is enabled, this mixin
 * prevents that packet from being sent, so the server cannot detect which
 * Fabric/mod channels are active on the client.
 *
 * Note: this suppresses ALL channel registration when enabled. A channel-level
 * granularity can be implemented once the exact CustomPayload serialization format
 * is confirmed for this MC version.
 */
@Mixin(Connection.class)
public class ModHiderMixin {

    @Inject(
        method = "sendPacket",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void skyos$filterChannelRegistration(Packet<?> packet,
                                                  ChannelFutureListener listener,
                                                  boolean flush,
                                                  CallbackInfo ci) {
        if (!ModHider.INSTANCE.isEnabled()) return;
        if (!(packet instanceof ServerboundCustomPayloadPacket custom)) return;

        // The payload ID for Fabric's channel registration is "minecraft:register".
        // Suppress the packet so the server does not see which mod channels are registered.
        String channelId = custom.payload().type().id().toString();
        if ("minecraft:register".equals(channelId) || "minecraft:unregister".equals(channelId)) {
            ci.cancel();
        }
    }
}
