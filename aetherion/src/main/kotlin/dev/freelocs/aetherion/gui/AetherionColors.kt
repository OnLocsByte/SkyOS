package dev.freelocs.aetherion.gui

/** Dark, clean color palette for the Aetherion config GUI. */
object AetherionColors {
    // Backgrounds
    const val BG_OVERLAY  = 0xCC000000.toInt()
    const val BG_PANEL    = 0xF2141420.toInt()
    const val BG_SIDEBAR  = 0xF50B0B16.toInt()
    const val BG_CARD     = 0xFF181828.toInt()
    const val BG_HOVER    = 0x0DFFFFFF.toInt()

    // Borders
    const val BORDER      = 0x14FFFFFF.toInt()
    const val BORDER_HI   = 0x1AFFFFFF.toInt()

    // Foregrounds
    const val FG_PRIMARY   = 0xFFEDF3FF.toInt()
    const val FG_SECONDARY = 0xA6EDF3FF.toInt()
    const val FG_MUTED     = 0x66EDF3FF.toInt()
    const val FG_DISABLED  = 0x59EDF3FF.toInt()

    // Aetherion brand gradient — sky-to-aether blues/violets
    const val BRAND_CYAN   = 0xFF3FE0E0.toInt()
    const val BRAND_SKY    = 0xFF3F9CFF.toInt()
    const val BRAND_BLUE   = 0xFF4F6FFF.toInt()
    const val BRAND_VIOLET = 0xFF9C5CFF.toInt()
    const val BRAND_PINK   = 0xFFD860FF.toInt()

    val BRAND_GRADIENT = listOf(BRAND_CYAN, BRAND_SKY, BRAND_BLUE, BRAND_VIOLET, BRAND_PINK)

    // Semantic
    const val SUCCESS = 0xFF4ADE80.toInt()
    const val WARNING = 0xFFF5A623.toInt()
    const val ERROR   = 0xFFFF6B6B.toInt()

    // Toggles
    const val TOGGLE_OFF  = 0x26FFFFFF.toInt()
    const val TOGGLE_ON_L = 0xFF3F9CFF.toInt()
    const val TOGGLE_ON_R = 0xFF9C5CFF.toInt()
    const val TOGGLE_KNOB = 0xFFFFFFFF.toInt()

    // Sidebar selection
    const val SIDEBAR_ACTIVE = 0x20FFFFFF.toInt()
    const val SIDEBAR_HOVER  = 0x0FFFFFFF.toInt()

    // One accent per top-level category
    val CATEGORY_ACCENTS = mapOf(
        "Dungeons"  to BRAND_VIOLET,
        "Skills"    to BRAND_CYAN,
        "Tooltips"  to BRAND_SKY,
        "Slayer"    to 0xFFFF6B6B.toInt(),
        "Best Flip" to 0xFFFFC24B.toInt()
    )

    // Skyblock rarity colors, used for tooltip frames
    val RARITY_COLORS = mapOf(
        "COMMON"        to 0xFFFFFFFF.toInt(),
        "UNCOMMON"      to 0xFF55FF55.toInt(),
        "RARE"          to 0xFF5555FF.toInt(),
        "EPIC"          to 0xFFAA00AA.toInt(),
        "LEGENDARY"     to 0xFFFFAA00.toInt(),
        "MYTHIC"        to 0xFFFF55FF.toInt(),
        "DIVINE"        to 0xFF55FFFF.toInt(),
        "SPECIAL"       to 0xFFFF5555.toInt(),
        "VERY SPECIAL"  to 0xFFFF5555.toInt(),
        "ADMIN"         to 0xFFAA0000.toInt()
    )
}
