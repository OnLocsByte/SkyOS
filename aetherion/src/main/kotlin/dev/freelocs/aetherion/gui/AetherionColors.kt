package dev.freelocs.aetherion.gui

/** Dark, clean color palette for the Aetherion config GUI. */
object AetherionColors {
    // Backgrounds
    const val BG_OVERLAY    = 0xCC000000.toInt()
    const val BG_PANEL      = 0xF2131320.toInt()
    const val BG_SIDEBAR    = 0xF50B0B16.toInt()
    const val BG_CARD       = 0xFF161726.toInt()
    const val BG_ELEVATED   = 0xFF1C1E30.toInt()

    // Borders
    const val BORDER        = 0x14FFFFFF.toInt()
    const val BORDER_HI     = 0x1AFFFFFF.toInt()

    // Foregrounds
    const val FG_PRIMARY    = 0xFFF1F3FF.toInt()
    const val FG_SECONDARY  = 0xA6F1F3FF.toInt()
    const val FG_MUTED      = 0x66F1F3FF.toInt()
    const val FG_DISABLED   = 0x59F1F3FF.toInt()

    // Brand gradient stops — "Aetherion" sky/aether theme
    const val BRAND_TEAL    = 0xFF19E3B0.toInt()
    const val BRAND_CYAN    = 0xFF22C7E8.toInt()
    const val BRAND_SKY     = 0xFF3E9BFF.toInt()
    const val BRAND_INDIGO  = 0xFF6C63FF.toInt()
    const val BRAND_VIOLET  = 0xFF9B5CFF.toInt()

    val BRAND_GRADIENT = listOf(BRAND_TEAL, BRAND_CYAN, BRAND_SKY, BRAND_INDIGO, BRAND_VIOLET)

    // Semantic
    const val SUCCESS       = 0xFF4ADE80.toInt()
    const val WARNING       = 0xFFF5A623.toInt()
    const val ERROR         = 0xFFFF6B6B.toInt()
    const val INFO          = 0xFF4F8EF7.toInt()

    // Toggles
    const val TOGGLE_OFF    = 0x26FFFFFF.toInt()
    const val TOGGLE_ON_L   = 0xFF3E9BFF.toInt()
    const val TOGGLE_ON_R   = 0xFF19E3B0.toInt()
    const val TOGGLE_KNOB   = 0xFFFFFFFF.toInt()

    // Sidebar selection
    const val SIDEBAR_ACTIVE = 0x20FFFFFF.toInt()
    const val SIDEBAR_HOVER  = 0x0FFFFFFF.toInt()

    // Item rarity colors (Hypixel Skyblock convention), used by ItemRarityTooltipFeature
    const val RARITY_COMMON     = 0xFFFFFFFF.toInt()
    const val RARITY_UNCOMMON   = 0xFF55FF55.toInt()
    const val RARITY_RARE       = 0xFF5555FF.toInt()
    const val RARITY_EPIC       = 0xFFAA00AA.toInt()
    const val RARITY_LEGENDARY  = 0xFFFFAA00.toInt()
    const val RARITY_MYTHIC     = 0xFFFF55FF.toInt()
    const val RARITY_DIVINE     = 0xFF55FFFF.toInt()
    const val RARITY_SPECIAL    = 0xFFFF5555.toInt()
    const val RARITY_VERY_SPECIAL = 0xFFFF5555.toInt()

    // Category accent colors (one per top-level feature category)
    val CATEGORY_ACCENTS = mapOf(
        "Dungeon Timer"    to BRAND_INDIGO,
        "Skill Progress"   to BRAND_TEAL,
        "Item Tooltips"    to BRAND_VIOLET,
        "Slayer HP Bar"    to BRAND_SKY,
        "Best Flip"        to BRAND_CYAN,
        "HUD"              to FG_DISABLED
    )
}
