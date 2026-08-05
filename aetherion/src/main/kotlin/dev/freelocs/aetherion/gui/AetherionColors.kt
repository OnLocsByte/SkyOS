package dev.freelocs.aetherion.gui

/**
 * Dark theme palette. Structurally mirrors SkyOS's config GUI (same layer
 * roles: overlay / panel / sidebar / card, same toggle + brand-gradient
 * treatment) but with Aetherion's own "aether sky" color identity instead
 * of reusing SkyOS's brand colors.
 */
object AetherionColors {
    // Backgrounds
    const val BG_OVERLAY    = 0xCC000000.toInt()
    const val BG_PANEL      = 0xF2141225.toInt()
    const val BG_SIDEBAR    = 0xF50B0A1A.toInt()
    const val BG_CARD       = 0xFF17162C.toInt()
    const val BG_ELEVATED   = 0xFF1D1B36.toInt()
    const val BG_HOVER      = 0x0DFFFFFF.toInt()
    const val BG_ACTIVE     = 0x15FFFFFF.toInt()

    // Borders
    const val BORDER        = 0x14FFFFFF.toInt()
    const val BORDER_HI     = 0x1AFFFFFF.toInt()

    // Foregrounds
    const val FG_PRIMARY    = 0xFFF1F0FF.toInt()
    const val FG_SECONDARY  = 0xA6F1F0FF.toInt()
    const val FG_MUTED      = 0x66F1F0FF.toInt()
    const val FG_DISABLED   = 0x59F1F0FF.toInt()

    // Brand gradient stops ("aether sky": violet -> indigo -> blue -> sky -> cyan -> teal)
    const val BRAND_VIOLET  = 0xFF7C3AED.toInt()
    const val BRAND_INDIGO  = 0xFF6366F1.toInt()
    const val BRAND_BLUE    = 0xFF3B82F6.toInt()
    const val BRAND_SKY     = 0xFF38BDF8.toInt()
    const val BRAND_CYAN    = 0xFF22D3EE.toInt()
    const val BRAND_TEAL    = 0xFF2DD4BF.toInt()

    val BRAND_GRADIENT = listOf(BRAND_VIOLET, BRAND_INDIGO, BRAND_BLUE, BRAND_SKY, BRAND_CYAN, BRAND_TEAL)

    // Semantic
    const val SUCCESS       = 0xFF4ADE80.toInt()
    const val WARNING       = 0xFFF5A623.toInt()
    const val ERROR         = 0xFFFF6B6B.toInt()
    const val INFO          = 0xFF4F8EF7.toInt()

    // Toggles
    const val TOGGLE_OFF    = 0x26FFFFFF.toInt()
    const val TOGGLE_ON_L   = 0xFF6366F1.toInt()
    const val TOGGLE_ON_R   = 0xFF22D3EE.toInt()
    const val TOGGLE_KNOB   = 0xFFFFFFFF.toInt()

    // Sidebar selection
    const val SIDEBAR_ACTIVE = 0x20FFFFFF.toInt()
    const val SIDEBAR_HOVER  = 0x0FFFFFFF.toInt()

    // Hypixel Skyblock item rarity colors (used by the tooltip rarity-frame feature)
    const val RARITY_COMMON        = 0xFFAAAAAA.toInt()
    const val RARITY_UNCOMMON      = 0xFF55FF55.toInt()
    const val RARITY_RARE          = 0xFF5555FF.toInt()
    const val RARITY_EPIC          = 0xFFAA00AA.toInt()
    const val RARITY_LEGENDARY     = 0xFFFFAA00.toInt()
    const val RARITY_MYTHIC        = 0xFFFF55FF.toInt()
    const val RARITY_DIVINE        = 0xFF55FFFF.toInt()
    const val RARITY_SPECIAL       = 0xFFFF5555.toInt()
    const val RARITY_VERY_SPECIAL  = 0xFFFF5555.toInt()
    const val RARITY_ADMIN         = 0xFFAA0000.toInt()
}
