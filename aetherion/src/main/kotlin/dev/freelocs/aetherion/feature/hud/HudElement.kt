package dev.freelocs.aetherion.feature.hud

import dev.freelocs.aetherion.config.HudPosition
import net.minecraft.client.gui.GuiGraphics

/** A HUD overlay that can be shown during gameplay and repositioned via the HUD editor. */
interface HudElement {
    val id: String
    val label: String

    fun isEnabled(): Boolean

    fun getPosition(): HudPosition
    fun savePosition()

    /** Approximate footprint in pixels at scale 1.0, used for the editor's drag box and click hit-testing. */
    fun previewSize(): Pair<Int, Int>

    /** Renders the live overlay during normal gameplay. */
    fun render(ctx: GuiGraphics)

    /** Renders a representative preview while the HUD editor is open (shown even without live data). */
    fun renderPreview(ctx: GuiGraphics, x: Int, y: Int)
}
