package dev.freelocs.aetherion.util

/**
 * Hypixel Skyblock renders a mob's current/max health directly in its
 * nametag (e.g. "Sven Packmaster §c12,345,678/50,000,000❤"). Reading that
 * text is the same mechanism the game's own floating health display uses,
 * so it's more reliable than trying to re-parse a boss's actionbar text.
 */
object SlayerPatterns {
    val NAMETAG_HEALTH_PATTERN = Regex(
        """^(.*?)\s*(?:§.)*([\d,]+)(?:§.)*/(?:§.)*([\d,]+)(?:§.)*❤?\s*$"""
    )

    val SLAYER_BOSS_NAME_HINTS = listOf(
        "Revenant", "Tarantula", "Sven", "Voidgloom", "Inferno Demonlord",
        "Riftstalker Bloodfiend", "Deformed", "Brood Mother",
    )

    fun parseHealth(nameTag: String): Triple<String, Long, Long>? {
        val match = NAMETAG_HEALTH_PATTERN.find(nameTag) ?: return null
        val name = match.groupValues[1].trim()
        val current = match.groupValues[2].replace(",", "").toLongOrNull() ?: return null
        val max = match.groupValues[3].replace(",", "").toLongOrNull() ?: return null
        if (max <= 0) return null
        return Triple(name, current, max)
    }
}
