package dev.freelocs.aetherion.features.tooltip

import dev.freelocs.aetherion.config.ConfigManager
import dev.freelocs.aetherion.gui.AetherionColors
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component

/**
 * Adds a rarity-colored divider (and optionally the vanilla item id) to
 * Skyblock item tooltips. Hypixel already colors the rarity line itself
 * (e.g. "§6§lLEGENDARY SWORD"); we just detect that keyword and echo the
 * matching color as a visual frame around the tooltip.
 */
object RarityTooltipFeature {

    fun init() {
        ItemTooltipCallback.EVENT.register { stack, _, _, lines ->
            val cfg = ConfigManager.config.tooltip
            if (!cfg.enabled) return@register

            val rarity = lines.firstNotNullOfOrNull { line -> findRarity(line.string) }

            if (rarity != null && cfg.rarityColorDivider) {
                val color = AetherionColors.RARITY_COLORS[rarity] ?: AetherionColors.RARITY_COLORS["COMMON"]!!
                val divider = Component.literal("▬".repeat(18)).withStyle { it.withColor(color) }
                lines.add(0, divider)
                lines.add(divider.copy())
            }

            if (cfg.showItemId) {
                val id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.item)
                lines.add(Component.literal(id.toString()).withStyle(ChatFormatting.DARK_GRAY))
            }
        }
    }

    private fun findRarity(text: String): String? {
        val upper = text.uppercase()
        return AetherionColors.RARITY_COLORS.keys.firstOrNull { upper.contains(it) }
    }
}
