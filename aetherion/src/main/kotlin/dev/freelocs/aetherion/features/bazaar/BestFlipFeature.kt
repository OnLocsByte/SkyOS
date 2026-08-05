package dev.freelocs.aetherion.features.bazaar

import dev.freelocs.aetherion.config.ConfigManager
import dev.freelocs.aetherion.gui.AetherionColors
import dev.freelocs.aetherion.hud.HudElement
import dev.freelocs.aetherion.hud.HudOverlayManager
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Display-only "best flip" price checker.
 *
 * Bazaar flips: ranks products by the current instant-buy/instant-sell
 * spread and shows the top [dev.freelocs.aetherion.config.BestFlipConfig.maxResults].
 *
 * Auction-house flips (optional, heuristic): tracks a session-local rolling
 * average lowest-BIN price per item name and flags new listings that are
 * unusually far below it. This intentionally ignores enchantments/reforges/
 * NBT — it is a rough first pass, not a precise valuation.
 *
 * Never buys or bids automatically; it only renders numbers already public
 * on the Hypixel API.
 */
object BestFlipFeature {

    private val logger = LoggerFactory.getLogger("Aetherion/BestFlip")
    private val executor = Executors.newSingleThreadExecutor { r -> Thread(r, "Aetherion-BestFlip").also { it.isDaemon = true } }
    private val refreshing = AtomicBoolean(false)

    data class BazaarFlip(val productId: String, val buyPrice: Double, val sellPrice: Double, val marginPercent: Double)
    data class AhFlip(val itemName: String, val price: Long, val averagePrice: Double, val discountPercent: Double)

    @Volatile private var bazaarFlips: List<BazaarFlip> = emptyList()
    @Volatile private var ahFlips: List<AhFlip> = emptyList()
    @Volatile private var lastError: String? = null
    @Volatile private var lastRefreshMs = 0L

    private val ahPriceHistory = mutableMapOf<String, MutableList<Long>>()
    private const val AH_HISTORY_SIZE = 50
    private const val AH_MIN_SAMPLES = 5
    private const val AH_DISCOUNT_THRESHOLD = 0.6 // flag prices below 60% of running average

    fun init() {
        ClientTickEvents.END_CLIENT_TICK.register {
            val cfg = ConfigManager.config.bestFlip
            if (!cfg.enabled || cfg.apiKey.isBlank()) return@register
            val now = System.currentTimeMillis()
            if (now - lastRefreshMs < cfg.refreshIntervalSeconds * 1000L) return@register
            refreshAsync()
        }

        HudOverlayManager.register(
            HudElement(
                id = "best_flip",
                displayName = "Best Flip",
                previewWidth = 170,
                previewHeight = 70,
                isEnabled = { ConfigManager.config.bestFlip.enabled },
                getPosition = { ConfigManager.config.bestFlip.position },
                setPosition = { ConfigManager.config.bestFlip.position = it },
                render = ::render
            )
        )
    }

    private fun refreshAsync() {
        if (!refreshing.compareAndSet(false, true)) return
        lastRefreshMs = System.currentTimeMillis()
        CompletableFuture.runAsync({
            val cfg = ConfigManager.config.bestFlip
            try {
                bazaarFlips = computeBazaarFlips(cfg.apiKey, cfg.minProfitPercent, cfg.maxResults)
                if (cfg.includeAuctionHouseHeuristic) {
                    ahFlips = computeAhFlips(cfg.apiKey, cfg.maxResults)
                }
                lastError = null
            } catch (e: Exception) {
                logger.warn("Best Flip refresh failed", e)
                lastError = e.message ?: "Request failed"
            } finally {
                refreshing.set(false)
            }
        }, executor)
    }

    private fun computeBazaarFlips(apiKey: String, minProfitPercent: Int, maxResults: Int): List<BazaarFlip> {
        return HypixelApiClient.fetchBazaar(apiKey)
            .filter { it.sellPrice > 0 && it.buyVolume > 0 && it.sellVolume > 0 }
            .map {
                val margin = (it.buyPrice - it.sellPrice) / it.sellPrice * 100.0
                BazaarFlip(it.productId, it.buyPrice, it.sellPrice, margin)
            }
            .filter { it.marginPercent >= minProfitPercent }
            .sortedByDescending { it.marginPercent }
            .take(maxResults)
    }

    private fun computeAhFlips(apiKey: String, maxResults: Int): List<AhFlip> {
        val auctions = HypixelApiClient.fetchAuctionsPage0(apiKey)
        val flips = mutableListOf<AhFlip>()

        for (auction in auctions) {
            val history = ahPriceHistory.getOrPut(auction.itemName) { mutableListOf() }
            val average = if (history.isNotEmpty()) history.average() else null

            if (average != null && history.size >= AH_MIN_SAMPLES && auction.startingBid < average * AH_DISCOUNT_THRESHOLD) {
                val discount = (1.0 - auction.startingBid / average) * 100.0
                flips.add(AhFlip(auction.itemName, auction.startingBid, average, discount))
            }

            history.add(auction.startingBid)
            while (history.size > AH_HISTORY_SIZE) history.removeAt(0)
        }

        return flips.sortedByDescending { it.discountPercent }.take(maxResults)
    }

    private fun render(ctx: GuiGraphics, x: Int, y: Int) {
        val font = Minecraft.getInstance().font
        val cfg = ConfigManager.config.bestFlip
        var line = y

        ctx.drawString(font, "§eBest Flip", x, line, AetherionColors.FG_PRIMARY, true)
        line += 10

        if (cfg.apiKey.isBlank()) {
            ctx.drawString(font, "§7Set API key in /aetherion", x, line, AetherionColors.FG_MUTED, true)
            return
        }
        lastError?.let {
            ctx.drawString(font, "§c$it", x, line, AetherionColors.ERROR, true)
            return
        }

        if (bazaarFlips.isEmpty()) {
            ctx.drawString(font, "§7No flips above threshold", x, line, AetherionColors.FG_MUTED, true)
        }
        for (flip in bazaarFlips) {
            val text = "§b${flip.productId} §a+%.1f%%".format(flip.marginPercent)
            ctx.drawString(font, text, x, line, AetherionColors.FG_SECONDARY, true)
            line += 10
        }
        for (flip in ahFlips) {
            val text = "§d${flip.itemName} §a-%.0f%%".format(flip.discountPercent)
            ctx.drawString(font, text, x, line, AetherionColors.FG_SECONDARY, true)
            line += 10
        }
    }
}
