package dev.freelocs.aetherion.util

import dev.freelocs.aetherion.gui.AetherionColors

/**
 * Hypixel Skyblock item rarity tiers. Skyblock items carry their rarity as
 * the last lore line (e.g. "§9RARE SWORD"), not as vanilla's own rarity
 * component, so it has to be parsed out of the tooltip text.
 */
enum class SkyblockRarity(val displayName: String, val color: Int) {
    COMMON("COMMON", AetherionColors.RARITY_COMMON),
    UNCOMMON("UNCOMMON", AetherionColors.RARITY_UNCOMMON),
    RARE("RARE", AetherionColors.RARITY_RARE),
    EPIC("EPIC", AetherionColors.RARITY_EPIC),
    LEGENDARY("LEGENDARY", AetherionColors.RARITY_LEGENDARY),
    MYTHIC("MYTHIC", AetherionColors.RARITY_MYTHIC),
    DIVINE("DIVINE", AetherionColors.RARITY_DIVINE),
    VERY_SPECIAL("VERY SPECIAL", AetherionColors.RARITY_VERY_SPECIAL),
    SPECIAL("SPECIAL", AetherionColors.RARITY_SPECIAL),
    ADMIN("ADMIN", AetherionColors.RARITY_ADMIN);

    companion object {
        // Longest names first so "VERY SPECIAL" is matched before the shorter "SPECIAL".
        private val byLength = entries.sortedByDescending { it.displayName.length }
        private val stripColorCodes = Regex("§.")

        fun fromLoreLine(line: String): SkyblockRarity? {
            val plain = stripColorCodes.replace(line, "").uppercase()
            return byLength.firstOrNull { plain.contains(it.displayName) }
        }

        /** Scans lore bottom-up, since Skyblock always places the rarity line last. */
        fun fromLore(loreLines: List<String>): SkyblockRarity? {
            for (line in loreLines.asReversed()) {
                val plain = stripColorCodes.replace(line, "").trim()
                if (plain.isEmpty()) continue
                return fromLoreLine(plain)
            }
            return null
        }
    }
}
