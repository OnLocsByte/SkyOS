package dev.skyos.mixin;

import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Chat filter mixin. Primary filtering is done via Fabric's
 * ClientReceiveMessageEvents in FirmamentAnnouncerRemover.kt.
 * This mixin is reserved for future use (e.g. HUD-injected messages
 * that bypass the Fabric event pipeline).
 */
@Mixin(ChatComponent.class)
public class ChatMixin {

    @Inject(
        method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void skyos$onAddMessage(Component message,
                                    MessageSignature signature,
                                    GuiMessageTag tag,
                                    CallbackInfo ci) {
        // Future: intercept HUD-inserted messages that bypass ClientReceiveMessageEvents.
    }
}
