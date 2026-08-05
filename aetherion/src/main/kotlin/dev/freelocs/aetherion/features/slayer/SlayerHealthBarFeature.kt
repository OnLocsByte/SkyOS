package dev.freelocs.aetherion.features.slayer

import dev.freelocs.aetherion.config.ConfigManager
import dev.freelocs.aetherion.features.AetherionFeature
import dev.freelocs.aetherion.features.ConfigOption
import dev.freelocs.aetherion.gui.AetherionColors
import dev.freelocs.aetherion.hud.HudElement
import dev.freelocs.aetherion.hud.HudManager
import dev.freelocs.aetherion.util.SlayerPatterns
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.gui.GuiGraphics

/**
 * Tracks the nearest Slayer boss by reading its nametag (Hypixel renders
 * live HP directly into the entity's display name) and shows it as a HUD
 * progress bar instead of squinting at the floating text above the mob.
 */
object SlayerHealthBarFeature : AetherionFeature {
    override val id = "slayer_health_bar"
    override val displayName = "Slayer Boss Health Bar"
    override val description = "Shows the nearest slayer boss's health as a bar overlay."
    override val category = "Slayer"

    private const val MAX_TRACK_DISTANCE = 40.0
    private const val TARGET_TIMEOUT_MILLIS = 5_000L

    override var enabled: Boolean
        get() = ConfigManager.config.slayerHealthBar.enabled
        set(value) {
            ConfigManager.config.slayerHealthBar.enabled = value
            ConfigManager.save()
        }

    override fun subOptions(): List<ConfigOption> {
        val cfg = ConfigManager.config.slayerHealthBar
        return listOf(
            ConfigOption.Toggle(
                "Show percentage",
                "Display the current HP percentage next to the bar",
                { cfg.showPercentage },
                { cfg.showPercentage = it; ConfigManager.save() }
            ),
            ConfigOption.Toggle(
                "Show boss name",
                "Display the boss's name above the bar",
                { cfg.showBossName },
                { cfg.showBossName = it; ConfigManager.save() }
            ),
        )
    }

    @Volatile private var bossName: String = ""
    @Volatile private var currentHp: Long = 0
    @Volatile private var maxHp: Long = 1
    @Volatile private var lastSeenMillis: Long = 0

    override fun init() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (!enabled) return@register
            val level = client.level ?: return@register
            val player = client.player ?: return@register

            var bestName: String? = null
            var bestCurrent = 0L
            var bestMax = 1L
            var bestDistSq = Double.MAX_VALUE

            for (entity in level.entitiesForRendering()) {
                if (!entity.hasCustomName()) continue
                val name = entity.displayName?.string ?: continue
                if (SlayerPatterns.SLAYER_BOSS_NAME_HINTS.none { name.contains(it, ignoreCase = true) }) continue
                val parsed = SlayerPatterns.parseHealth(name) ?: continue
                val distSq = entity.distanceToSqr(player)
                if (distSq > MAX_TRACK_DISTANCE * MAX_TRACK_DISTANCE) continue
                if (distSq < bestDistSq) {
                    bestDistSq = distSq
                    bestName = parsed.first
                    bestCurrent = parsed.second
                    bestMax = parsed.third
                }
            }

            if (bestName != null) {
                bossName = bestName
                currentHp = bestCurrent
                maxHp = bestMax
                lastSeenMillis = System.currentTimeMillis()
            } else if (System.currentTimeMillis() - lastSeenMillis > TARGET_TIMEOUT_MILLIS) {
                bossName = ""
            }
        }

        HudManager.register(SlayerHealthBarHud)
    }

    fun hasTarget(): Boolean = bossName.isNotEmpty()

    private object SlayerHealthBarHud : HudElement {
        override val id = "slayer_health_bar"
        override val label = "Slayer Boss Health"

        private const val BAR_W = 160
        private const val BAR_H = 12

        override fun isFeatureEnabled(): Boolean = enabled
        override fun hasLiveData(): Boolean = hasTarget()

        override fun contentSize(): Pair<Int, Int> = BAR_W to (BAR_H + 12)

        override fun render(context: GuiGraphics, x: Int, y: Int) {
            val cfg = ConfigManager.config.slayerHealthBar
            val mc = net.minecraft.client.Minecraft.getInstance()

            val fraction = if (hasTarget()) (currentHp.toDouble() / maxHp.toDouble()).coerceIn(0.0, 1.0) else 1.0
            var barY = y
            if (cfg.showBossName) {
                val name = if (hasTarget()) bossName else "Slayer Boss"
                context.drawString(mc.font, name, x, barY, AetherionColors.FG_PRIMARY, true)
                barY += 12
            }

            context.fill(x, barY, x + BAR_W, barY + BAR_H, AetherionColors.BG_ELEVATED)
            val fillW = (BAR_W * fraction).toInt()
            if (fillW > 0) {
                context.fill(x, barY, x + fillW, barY + BAR_H, AetherionColors.ERROR)
            }
            context.fill(x, barY, x + BAR_W, barY + 1, AetherionColors.BORDER_HI)
            context.fill(x, barY + BAR_H - 1, x + BAR_W, barY + BAR_H, AetherionColors.BORDER_HI)

            if (cfg.showPercentage) {
                val pct = "${(fraction * 100).toInt()}%"
                context.drawString(mc.font, pct, x + (BAR_W - mc.font.width(pct)) / 2, barY + 2, AetherionColors.FG_PRIMARY, true)
            }
        }
    }
}
