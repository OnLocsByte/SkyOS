package dev.freelocs.aetherion.hud

import dev.freelocs.aetherion.config.ConfigManager
import dev.freelocs.aetherion.config.HudPositionConfig
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.Minecraft

/** Registry + renderer for all draggable HUD elements. */
object HudManager {
    private val elements = mutableListOf<HudElement>()

    val registered: List<HudElement> get() = elements

    fun register(element: HudElement) {
        elements += element
    }

    fun positionOf(id: String): HudPositionConfig =
        ConfigManager.config.hudPositions.getOrPut(id) { HudPositionConfig() }

    fun init() {
        HudRenderCallback.EVENT.register { context, _ ->
            val mc = Minecraft.getInstance()
            if (mc.options.hideGui) return@register
            for (element in elements) {
                if (!element.isFeatureEnabled() || !element.hasLiveData()) continue
                val pos = positionOf(element.id)
                val x = (pos.xFraction * mc.window.guiScaledWidth).toInt()
                val y = (pos.yFraction * mc.window.guiScaledHeight).toInt()

                context.pose().pushPose()
                context.pose().translate(x.toFloat(), y.toFloat(), 0f)
                context.pose().scale(pos.scale, pos.scale, 1f)
                element.render(context, 0, 0)
                context.pose().popPose()
            }
        }
    }
}
