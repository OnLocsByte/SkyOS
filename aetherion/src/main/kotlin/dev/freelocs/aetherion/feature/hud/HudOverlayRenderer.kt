package dev.freelocs.aetherion.feature.hud

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.Minecraft

/** Draws every enabled, registered [HudElement] each frame. Editing happens in [HudEditScreen]. */
object HudOverlayRenderer {

    fun init() {
        HudRenderCallback.EVENT.register { ctx, _ ->
            if (Minecraft.getInstance().options.hideGui) return@register
            HudManager.all().forEach { element ->
                if (element.isEnabled()) {
                    runCatching { element.render(ctx) }
                }
            }
        }
    }
}
