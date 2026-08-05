package dev.freelocs.aetherion.hud

import dev.freelocs.aetherion.config.HudPosition
import net.minecraft.client.gui.GuiGraphics

/**
 * A single draggable HUD overlay registered by a feature module.
 *
 * [previewWidth]/[previewHeight] size the placeholder box shown in the
 * HUD editor; the real overlay may render at a different size once it
 * has live data (e.g. a longer skill name).
 */
class HudElement(
    val id: String,
    val displayName: String,
    val previewWidth: Int,
    val previewHeight: Int,
    val isEnabled: () -> Boolean,
    val getPosition: () -> HudPosition,
    val setPosition: (HudPosition) -> Unit,
    val render: (GuiGraphics, x: Int, y: Int) -> Unit
)

/** Central registry all HUD-rendering features register themselves with. */
object HudOverlayManager {
    private val elements = mutableListOf<HudElement>()

    fun register(element: HudElement) {
        elements.removeAll { it.id == element.id }
        elements.add(element)
    }

    fun all(): List<HudElement> = elements

    fun renderEnabled(ctx: GuiGraphics, screenW: Int, screenH: Int) {
        for (element in elements) {
            if (!element.isEnabled()) continue
            val pos = element.getPosition()
            val x = (pos.x * screenW).toInt()
            val y = (pos.y * screenH).toInt()
            element.render(ctx, x, y)
        }
    }
}
