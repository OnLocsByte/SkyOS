package dev.freelocs.aetherion.feature.bazaar

import dev.freelocs.aetherion.config.ConfigManager
import dev.freelocs.aetherion.config.HudPosition
import dev.freelocs.aetherion.feature.Feature
import dev.freelocs.aetherion.feature.hud.HudElement
import dev.freelocs.aetherion.feature.hud.HudManager
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Periodically pulls Bazaar/AH prices off the client thread and surfaces the highest-margin
 * items. Display only: this never places bids, buy orders, or purchases on the player's behalf.
 */
object BestFlipFeature : Feature, HudElement {

    override val id = "best_flip"
    override val displayName = "Best Flip"

    private val executor = Executors.newSingleThreadExecutor { r -> Thread(r, "Aetherion-BestFlip").apply { isDaemon = true } }
    private val refreshing = AtomicBoolean(false)

    @Volatile
    var results: List<FlipResult> = emptyList()
        private set

    @Volatile
    var lastRefreshMs: Long = 0L
        private set

    @Volatile
    var lastError: String? = null
        private set

    override fun isEnabled(): Boolean = ConfigManager.config.bestFlip.enabled

    override fun init() {
        HudManager.register(this)

        ClientTickEvents.END_CLIENT_TICK.register {
            if (!isEnabled()) return@register
            val cfg = ConfigManager.config.bestFlip
            val dueMs = cfg.refreshIntervalSeconds * 1000L
            if (System.currentTimeMillis() - lastRefreshMs >= dueMs) {
                refreshAsync()
            }
        }
    }

    fun refreshAsync() {
        if (!refreshing.compareAndSet(false, true)) return
        val cfg = ConfigManager.config.bestFlip
        executor.submit {
            try {
                val combined = mutableListOf<FlipResult>()
                if (cfg.scanBazaar) combined += HypixelApiClient.fetchBazaar()
                if (cfg.scanAuctionHouse) combined += HypixelApiClient.fetchAuctionFlips()

                results = combined
                    .filter { it.marginPerUnit >= cfg.minProfitPerUnit }
                    .sortedByDescending { it.marginPerUnit }
                    .take(cfg.maxResults)
                lastError = null
            } catch (e: Exception) {
                lastError = e.message ?: e.javaClass.simpleName
            } finally {
                lastRefreshMs = System.currentTimeMillis()
                refreshing.set(false)
            }
        }
    }

    // ── HudElement: compact top-3 panel ─────────────────────────────────────────

    override fun getPosition(): HudPosition = ConfigManager.config.bestFlip.hudPosition
    override fun savePosition() = ConfigManager.save()
    override fun previewSize(): Pair<Int, Int> = 140 to 44

    override fun render(ctx: GuiGraphics) {
        if (!ConfigManager.config.bestFlip.showHudOverlay) return
        val pos = getPosition()
        val font = Minecraft.getInstance().font
        ctx.drawString(font, "§bBest Flips", pos.x, pos.y, 0xFFFFFF, true)
        var y = pos.y + 10
        if (results.isEmpty()) {
            ctx.drawString(font, "§7No data yet", pos.x, y, 0xFFFFFF, true)
            return
        }
        results.take(3).forEach { flip ->
            ctx.drawString(font, "§f${flip.itemId.take(16)} §a+${flip.marginPerUnit.toLong()}", pos.x, y, 0xFFFFFF, true)
            y += 10
        }
    }

    override fun renderPreview(ctx: GuiGraphics, x: Int, y: Int) {
        val font = Minecraft.getInstance().font
        ctx.drawString(font, "§bBest Flips", x, y, 0xFFFFFF, true)
        ctx.drawString(font, "§fENCHANTED_X §a+120,000", x, y + 10, 0xFFFFFF, true)
    }
}
