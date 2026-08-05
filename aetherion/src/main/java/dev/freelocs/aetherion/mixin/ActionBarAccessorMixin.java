package dev.freelocs.aetherion.mixin;

import dev.freelocs.aetherion.util.ActionBarTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Read-only tap on the vanilla action bar.
 * <p>
 * This is the ONLY mixin in Aetherion. It injects at the head of
 * {@code Gui.setOverlayMessage} purely to copy the text into
 * {@link ActionBarTracker} for our HUD parsers (dungeon timer, skill XP,
 * slayer HP). It is {@code cancellable = false} and never touches the
 * network layer — it does not intercept, cancel or modify any packet, and
 * the vanilla action bar still renders exactly as before. Do not add
 * mixins that touch {@code Connection}/packet handling to this mod.
 */
@Mixin(Gui.class)
public class ActionBarAccessorMixin {

    @Inject(method = "setOverlayMessage", at = @At("HEAD"), require = 0)
    private void aetherion$onSetOverlayMessage(Component message, boolean animateColor, CallbackInfo ci) {
        ActionBarTracker.INSTANCE.onActionBarMessage(message.getString());
    }
}
