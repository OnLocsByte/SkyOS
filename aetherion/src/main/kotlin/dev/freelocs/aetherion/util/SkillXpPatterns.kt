package dev.freelocs.aetherion.util

/**
 * Parsing helpers for Hypixel Skyblock's action-bar skill XP gain messages
 * (e.g. "+10 Farming (1,234/5,000,000)"). Hypixel occasionally tweaks the
 * exact spacing/icon glyph between updates; this pattern targets the
 * "+<amount> <Skill>" prefix that actually drives the overlay and treats
 * the progress fraction as optional so a formatting change degrades
 * gracefully instead of breaking XP tracking outright.
 */
object SkillXpPatterns {
    val SKILL_NAMES = listOf(
        "Farming", "Mining", "Combat", "Foraging", "Fishing",
        "Enchanting", "Alchemy", "Taming", "Carpentry", "Runecrafting", "Social"
    )

    val XP_GAIN_PATTERN = Regex(
        """\+([\d,.]+)\s*(?:XP\s*)?(${SKILL_NAMES.joinToString("|")})\b(?:\s*\(([\d,]+)/([\d,]+)\))?"""
    )

    fun parseAmount(raw: String): Double = raw.replace(",", "").toDoubleOrNull() ?: 0.0
}
