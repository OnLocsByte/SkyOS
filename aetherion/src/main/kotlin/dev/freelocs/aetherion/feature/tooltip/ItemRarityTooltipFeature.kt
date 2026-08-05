package dev.freelocs.aetherion.feature.tooltip

import dev.freelocs.aetherion.config.ConfigManager
import dev.freelocs.aetherion.feature.Feature
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.minecraft.network.chat.Component

/**
 * Skyblock items already encode rarity as colored text in their lore (e.g. "§6LEGENDARY SWORD"),
 * which vanilla renders as-is — this feature makes it easier to scan by adding a subtle rule
 * around the rarity line and, optionally, a plain "Reforge:" info line.
 *
 * Detection is purely text-based (reading the tooltip Hypixel already sent), so it never touches
 * item NBT directly and degrades to a no-op on non-Skyblock items. It only ever *adds* new lines —
 * it never rewrites an existing line, so original colors/formatting are never lost.
 */
object ItemRarityTooltipFeature : Feature {

    override val id = "item_rarity_tooltip"
    override val displayName = "Item Tooltips"

    private val RARITY_PATTERN = Regex(
        """\b(COMMON|UNCOMMON|RARE|EPIC|LEGENDARY|MYTHIC|DIVINE|SPECIAL|VERY SPECIAL|ADMIN)\b"""
    )

    override fun isEnabled(): Boolean = ConfigManager.config.itemRarityTooltip.enabled

    override fun init() {
        ItemTooltipCallback.EVENT.register { _, _, _, lines ->
            if (!isEnabled() || lines.isEmpty()) return@register
            val cfg = ConfigManager.config.itemRarityTooltip

            val hasRarityLine = lines.any { RARITY_PATTERN.containsMatchIn(it.string) }
            if (!hasRarityLine) return@register

            if (cfg.coloredBorder) {
                val rule = "§8${"▬".repeat(24)}"
                lines.add(1.coerceAtMost(lines.size), Component.literal(rule))
                lines.add(Component.literal(rule))
            }

            if (cfg.highlightReforge) {
                val nameWords = lines[0].string.trim().split(" ")
                if (nameWords.size > 2) {
                    // Most reforged Skyblock items are named "<Reforge> <Item Name>"; surface the
                    // reforge word as its own info line rather than rewriting the (colored) name line.
                    lines.add(1.coerceAtMost(lines.size), Component.literal("§7Reforge: §f${nameWords[0]}"))
                }
            }
        }
    }
}
