package dev.freelocs.aetherion.features.bazaar

import dev.freelocs.aetherion.config.ConfigManager
import dev.freelocs.aetherion.features.AetherionFeature
import dev.freelocs.aetherion.features.ConfigOption
import dev.freelocs.aetherion.gui.AetherionColors
import dev.freelocs.aetherion.hud.HudElement
import dev.freelocs.aetherion.hud.HudManager
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import java.util.Locale

/**
 * Display-only Bazaar flip checker. Periodically pulls Hypixel's public
 * Bazaar prices (no API key needed) and lists products with the largest
 * instant-buy/instant-sell spread — a rough signal for where a limit-order
 * flip could be worth placing. It never buys, sells, or places any order
 * itself.
 */
object BestFlipFeature : AetherionFeature {
    override val id = "best_flip"
    override val displayName = "Best Flip Price Checker"
    override val description = "Highlights bazaar products with a large buy/sell spread. Display only."
    override val category = "Economy"

    override var enabled: Boolean
        get() = ConfigManager.config.bestFlip.enabled
        set(value) {
            ConfigManager.config.bestFlip.enabled = value
            ConfigManager.save()
        }

    // Thresholds (min profit, margin, refresh interval, result count) are numeric
    // rather than toggles; they use their config defaults for now and are the
    // natural place to add slider-style ConfigOptions in a future pass.
    override fun subOptions(): List<ConfigOption> = emptyList()

    private var ticksUntilRefresh = 0

    override fun init() {
        ClientTickEvents.END_CLIENT_TICK.register {
            if (!enabled) return@register
            if (ticksUntilRefresh <= 0) {
                val cfg = ConfigManager.config.bestFlip
                BazaarPriceService.refreshAsync(cfg.minProfitPerFlip.toDouble(), cfg.minProfitMargin, cfg.maxResultsShown)
                ticksUntilRefresh = cfg.refreshIntervalSeconds * 20
            } else {
                ticksUntilRefresh--
            }
        }

        HudManager.register(BestFlipHud)
    }

    private object BestFlipHud : HudElement {
        override val id = "best_flip"
        override val label = "Best Flip Checker"

        override fun isFeatureEnabled(): Boolean = enabled
        override fun hasLiveData(): Boolean = BazaarPriceService.lastFlips.isNotEmpty() || BazaarPriceService.lastError != null

        override fun contentSize(): Pair<Int, Int> {
            val rows = BazaarPriceService.lastFlips.size.coerceAtLeast(1)
            return 200 to (14 + rows * 11)
        }

        override fun render(context: GuiGraphics, x: Int, y: Int) {
            val font = Minecraft.getInstance().font
            context.drawString(font, "Bazaar Flips", x, y, AetherionColors.BRAND_VIOLET, true)

            val error = BazaarPriceService.lastError
            val flips = BazaarPriceService.lastFlips
            var lineY = y + 12
            if (error != null && flips.isEmpty()) {
                context.drawString(font, "Error: $error", x, lineY, AetherionColors.ERROR, true)
                return
            }
            if (flips.isEmpty()) {
                context.drawString(font, "No flips match your filters yet", x, lineY, AetherionColors.FG_MUTED, true)
                return
            }
            for (flip in flips) {
                val name = flip.productId.replace('_', ' ').lowercase(Locale.US)
                    .replaceFirstChar { it.uppercase() }
                val text = "$name  +${formatCoins(flip.netProfitPerUnit)} (${formatPercent(flip.marginFraction)})"
                context.drawString(font, text, x, lineY, AetherionColors.SUCCESS, true)
                lineY += 11
            }
        }

        private fun formatCoins(value: Double): String = "%,.0f".format(Locale.US, value)
        private fun formatPercent(value: Double): String = "%.1f%%".format(Locale.US, value * 100)
    }
}
