package dev.freelocs.aetherion.config

/**
 * Anchor position of a HUD element, stored as a fraction (0..1) of the
 * screen width/height so the layout survives window resizes.
 */
data class HudPosition(
    var x: Float = 0.02f,
    var y: Float = 0.02f
)
