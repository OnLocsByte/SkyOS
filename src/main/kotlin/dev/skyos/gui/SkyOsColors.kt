package dev.skyos.gui

object SkyOsColors {
    // Backgrounds
    const val BG_OVERLAY    = 0xCC000000.toInt()
    const val BG_PANEL      = 0xF212121F.toInt()
    const val BG_SIDEBAR    = 0xF50D0D1A.toInt()
    const val BG_CARD       = 0xFF16172A.toInt()
    const val BG_ELEVATED   = 0xFF1C1D32.toInt()
    const val BG_HOVER      = 0x0DFFFFFF.toInt()
    const val BG_ACTIVE     = 0x15FFFFFF.toInt()

    // Borders
    const val BORDER        = 0x14FFFFFF.toInt()
    const val BORDER_HI     = 0x1AFFFFFF.toInt()

    // Foregrounds
    const val FG_PRIMARY    = 0xFFF0F0FF.toInt()
    const val FG_SECONDARY  = 0xA6F0F0FF.toInt()
    const val FG_MUTED      = 0x66F0F0FF.toInt()
    const val FG_DISABLED   = 0x59F0F0FF.toInt()

    // Brand gradient stops
    const val BRAND_GOLD    = 0xFFFFB800.toInt()
    const val BRAND_ORANGE  = 0xFFFF7A00.toInt()
    const val BRAND_PINK    = 0xFFFF4ECD.toInt()
    const val BRAND_MAGENTA = 0xFFD83FFF.toInt()
    const val BRAND_PURPLE  = 0xFF7B3FFF.toInt()
    const val BRAND_CYAN    = 0xFF00D4FF.toInt()
    const val BRAND_SKY     = 0xFF00AAFF.toInt()

    val BRAND_GRADIENT = listOf(BRAND_GOLD, BRAND_ORANGE, BRAND_PINK, BRAND_MAGENTA, BRAND_PURPLE, BRAND_CYAN, BRAND_SKY)

    // Semantic
    const val SUCCESS       = 0xFF4ADE80.toInt()
    const val WARNING       = 0xFFF5A623.toInt()
    const val ERROR         = 0xFFFF6B6B.toInt()
    const val INFO          = 0xFF4F8EF7.toInt()

    // Toggles
    const val TOGGLE_OFF    = 0x26FFFFFF.toInt()
    const val TOGGLE_ON_L   = 0xFF7B3FFF.toInt()
    const val TOGGLE_ON_R   = 0xFF00D4FF.toInt()
    const val TOGGLE_KNOB   = 0xFFFFFFFF.toInt()

    // Sidebar selection
    const val SIDEBAR_ACTIVE = 0x20FFFFFF.toInt()
    const val SIDEBAR_HOVER  = 0x0FFFFFFF.toInt()

    // Category accent colors (one per top-level category)
    val CATEGORY_ACCENTS = mapOf(
        "General" to BRAND_CYAN,
        "GUI"     to BRAND_PURPLE,
        "Visuals" to BRAND_PINK,
        "Main"    to BRAND_GOLD,
        "Chat"    to BRAND_SKY,
        "Misc"    to BRAND_ORANGE,
        "Dev"     to FG_DISABLED
    )
}
