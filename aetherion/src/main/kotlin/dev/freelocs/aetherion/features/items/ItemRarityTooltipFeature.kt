package dev.freelocs.aetherion.features.items

import dev.freelocs.aetherion.config.ConfigManager
import dev.freelocs.aetherion.features.AetherionFeature
import dev.freelocs.aetherion.features.ConfigOption
import dev.freelocs.aetherion.util.SkyblockRarity
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component

/**
 * Highlights a Skyblock item's rarity directly in its tooltip: recolors
 * the item name to the rarity's color and/or inserts a colored separator
 * line, so rarity is readable at a glance instead of buried in the lore.
 */
object ItemRarityTooltipFeature : AetherionFeature {
    override val id = "item_rarity_tooltip"
    override val displayName = "Item Rarity Tooltips"
    override val description = "Highlights a Skyblock item's rarity in its tooltip."
    override val category = "Items"

    override var enabled: Boolean
        get() = ConfigManager.config.itemRarityTooltip.enabled
        set(value) {
            ConfigManager.config.itemRarityTooltip.enabled = value
            ConfigManager.save()
        }

    override fun subOptions(): List<ConfigOption> {
        val cfg = ConfigManager.config.itemRarityTooltip
        return listOf(
            ConfigOption.Toggle(
                "Color item name",
                "Recolor the tooltip's first line to match its rarity",
                { cfg.colorItemName },
                { cfg.colorItemName = it; ConfigManager.save() }
            ),
            ConfigOption.Toggle(
                "Rarity separator",
                "Insert a colored separator line below the item name",
                { cfg.showRarityBorder },
                { cfg.showRarityBorder = it; ConfigManager.save() }
            ),
        )
    }

    override fun init() {
        ItemTooltipCallback.EVENT.register { _, _, _, lines ->
            if (!enabled || lines.isEmpty()) return@register
            val cfg = ConfigManager.config.itemRarityTooltip

            val loreStrings = lines.drop(1).map { it.string }
            val rarity = SkyblockRarity.fromLore(loreStrings) ?: return@register
            val formatting = closestFormatting(rarity)

            if (cfg.colorItemName) {
                lines[0] = Component.literal(lines[0].string).withStyle(formatting, ChatFormatting.BOLD)
            }
            if (cfg.showRarityBorder) {
                lines.add(1, Component.literal("▬▬▬▬▬▬▬▬▬▬▬▬").withStyle(formatting))
            }
        }
    }

    private fun closestFormatting(rarity: SkyblockRarity): ChatFormatting = when (rarity) {
        SkyblockRarity.COMMON -> ChatFormatting.WHITE
        SkyblockRarity.UNCOMMON -> ChatFormatting.GREEN
        SkyblockRarity.RARE -> ChatFormatting.BLUE
        SkyblockRarity.EPIC -> ChatFormatting.DARK_PURPLE
        SkyblockRarity.LEGENDARY -> ChatFormatting.GOLD
        SkyblockRarity.MYTHIC -> ChatFormatting.LIGHT_PURPLE
        SkyblockRarity.DIVINE -> ChatFormatting.AQUA
        SkyblockRarity.VERY_SPECIAL, SkyblockRarity.SPECIAL -> ChatFormatting.RED
        SkyblockRarity.ADMIN -> ChatFormatting.DARK_RED
    }
}
