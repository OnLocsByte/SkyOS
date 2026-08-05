package dev.freelocs.aetherion.features.slayer

import dev.freelocs.aetherion.config.ConfigManager
import dev.freelocs.aetherion.gui.AetherionColors
import dev.freelocs.aetherion.hud.HudElement
import dev.freelocs.aetherion.hud.HudOverlayManager
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics

/**
 * Replaces Hypixel's slayer-boss action-bar HP text with a HUD progress bar.
 *
 * Slayer bosses (Revenant, Tarantula, Sven, Voidgloom, Riftstalker, Bladesoul
 * ...) print their name and remaining/total health into the action bar while
 * they're alive. We match on that and render a bar instead of raw numbers.
 */
object SlayerHealthBarFeature {

    private val BOSS_NAMES = listOf(
        "Revenant", "Tarantula", "Sven", "Voidgloom", "Inferno",
        "Riftstalker", "Bladesoul", "Broodfather", "Seraph", "Werewolf"
    )
    private val HP_LINE_REGEX = Regex("""^(.+?):\s*[^\d]*([\d,]+)\s*/\s*([\d,]+)""")

    private data class BossState(val name: String, val current: Long, val max: Long, val lastSeenMs: Long)

    @Volatile private var active: BossState? = null

    fun init() {
        ClientReceiveMessageEvents.ALLOW_GAME.register { message, overlay ->
            val cfg = ConfigManager.config.slayerHp
            if (!overlay || !cfg.enabled) return@register true

            val parsed = tryParse(message.string)
            if (parsed != null) {
                active = parsed
                return@register !cfg.hideVanillaActionBarText
            }
            true
        }

        ClientTickEvents.END_CLIENT_TICK.register {
            val current = active ?: return@register
            if (System.currentTimeMillis() - current.lastSeenMs > 3000) active = null
        }

        HudOverlayManager.register(
            HudElement(
                id = "slayer_hp",
                displayName = "Slayer Boss HP",
                previewWidth = 160,
                previewHeight = 24,
                isEnabled = { ConfigManager.config.slayerHp.enabled && active != null },
                getPosition = { ConfigManager.config.slayerHp.position },
                setPosition = { ConfigManager.config.slayerHp.position = it },
                render = ::render
            )
        )
    }

    private fun tryParse(text: String): BossState? {
        if (BOSS_NAMES.none { text.contains(it, ignoreCase = true) }) return null
        val match = HP_LINE_REGEX.find(text) ?: return null
        val name = match.groupValues[1].trim()
        val current = match.groupValues[2].replace(",", "").toLongOrNull() ?: return null
        val max = match.groupValues[3].replace(",", "").toLongOrNull() ?: return null
        if (max <= 0) return null
        return BossState(name, current, max, System.currentTimeMillis())
    }

    private fun render(ctx: GuiGraphics, x: Int, y: Int) {
        val boss = active ?: return
        val font = Minecraft.getInstance().font
        val cfg = ConfigManager.config.slayerHp
        val progress = (boss.current.toDouble() / boss.max).coerceIn(0.0, 1.0)

        ctx.drawString(font, "§c${boss.name}", x, y, AetherionColors.FG_PRIMARY, true)

        val barWidth = 150
        val barY = y + 10
        ctx.fill(x, barY, x + barWidth, barY + 6, AetherionColors.TOGGLE_OFF)
        val filled = (barWidth * progress).toInt().coerceIn(0, barWidth)
        if (filled > 0) ctx.fill(x, barY, x + filled, barY + 6, AetherionColors.ERROR)

        if (cfg.showPercentage) {
            val text = "%.1f%%".format(progress * 100)
            ctx.drawString(font, text, x + barWidth + 4, barY - 2, AetherionColors.FG_SECONDARY, true)
        }
    }
}
