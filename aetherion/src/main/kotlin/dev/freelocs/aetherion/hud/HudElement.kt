package dev.freelocs.aetherion.hud

import net.minecraft.client.gui.GuiGraphics

/**
 * A single draggable HUD overlay contributed by a feature. HudManager owns
 * placement (position, drag editor); the element itself only knows how to
 * measure and draw its own content.
 */
interface HudElement {
    val id: String
    val label: String

    /** Whether the owning feature is switched on at all. Gates both live rendering and the drag editor. */
    fun isFeatureEnabled(): Boolean

    /**
     * Whether there's real data to show right now (e.g. an active slayer fight).
     * Only gates the *live* HUD — the drag editor still shows the element (using
     * whatever placeholder/default state [render] falls back to) so it can be
     * positioned even when nothing is currently happening in-game.
     */
    fun hasLiveData(): Boolean = true

    /** Content size in pixels at scale 1.0 — used for the drag editor's hitbox and default layout. */
    fun contentSize(): Pair<Int, Int>

    /** Draws the element's content with its top-left corner at (x, y). */
    fun render(context: GuiGraphics, x: Int, y: Int)
}
