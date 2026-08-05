package dev.freelocs.aetherion.feature.hud

/** Registry of all draggable HUD elements, shared by the in-game overlay renderer and the HUD editor screen. */
object HudManager {
    private val elements = mutableListOf<HudElement>()

    fun register(element: HudElement) {
        elements.add(element)
    }

    fun all(): List<HudElement> = elements
}
