package dev.freelocs.aetherion.feature.slayer

import dev.freelocs.aetherion.config.ConfigManager
import dev.freelocs.aetherion.config.HudPosition
import dev.freelocs.aetherion.feature.Feature
import dev.freelocs.aetherion.feature.hud.HudElement
import dev.freelocs.aetherion.feature.hud.HudManager
import dev.freelocs.aetherion.util.ActionBarTracker
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics

/**
 * Replaces Hypixel's raw "Slayer Boss: 1,234,567/50,000,000" action-bar text with a bar.
 * Purely a rendering aid — the vanilla action bar text is left untouched, we only read it.
 *
 * If Hypixel changes the wording, adjust [HP_PATTERN].
 */
object SlayerHealthBarFeature : Feature, HudElement {

    override val id = "slayer_health_bar"
    override val displayName = "Slayer HP Bar"

    private val HP_PATTERN = Regex("""(?:Slayer Boss|Boss)[^\d]*?([\d,]+) */ *([\d,]+)""", RegexOption.IGNORE_CASE)

    private var current: Long = 0
    private var max: Long = 1
    private var lastSeenMs: Long = 0

    override fun isEnabled(): Boolean = ConfigManager.config.slayerHealthBar.enabled

    override fun init() {
        HudManager.register(this)
        var lastParsedMs = 0L

        ClientTickEvents.END_CLIENT_TICK.register {
            if (!isEnabled()) return@register
            if (ActionBarTracker.lastUpdateMs == lastParsedMs) return@register
            lastParsedMs = ActionBarTracker.lastUpdateMs

            val match = HP_PATTERN.find(ActionBarTracker.lastMessage) ?: return@register
            val cur = match.groupValues[1].replace(",", "").toLongOrNull() ?: return@register
            val mx = match.groupValues[2].replace(",", "").toLongOrNull() ?: return@register
            if (mx <= 0) return@register
            current = cur
            max = mx
            lastSeenMs = System.currentTimeMillis()
        }
    }

    // ── HudElement ────────────────────────────────────────────────────────────

    override fun getPosition(): HudPosition = ConfigManager.config.slayerHealthBar.position
    override fun savePosition() = ConfigManager.save()
    override fun previewSize(): Pair<Int, Int> = 120 to 16

    override fun render(ctx: GuiGraphics) {
        if (System.currentTimeMillis() - lastSeenMs > 4000) return
        val cfg = ConfigManager.config.slayerHealthBar
        val pos = getPosition()
        val font = Minecraft.getInstance().font
        val barW = 120
        val barH = 6
        val pct = (current.toDouble() / max.toDouble()).coerceIn(0.0, 1.0)

        ctx.fill(pos.x, pos.y, pos.x + barW, pos.y + barH, 0x40000000)
        ctx.fill(pos.x, pos.y, pos.x + (barW * pct).toInt(), pos.y + barH, 0xFFE84C4C.toInt())
        drawBorder(ctx, pos.x, pos.y, barW, barH, 0x80000000.toInt())

        val label = buildString {
            append("§cSlayer Boss")
            if (cfg.showPercentage) append(" §7${"%.1f".format(pct * 100)}%")
            if (cfg.showAbsoluteHp) append(" §8($current/$max)")
        }
        ctx.drawString(font, label, pos.x, pos.y + barH + 2, 0xFFFFFF, true)
    }

    override fun renderPreview(ctx: GuiGraphics, x: Int, y: Int) {
        val font = Minecraft.getInstance().font
        ctx.fill(x, y, x + 120, y + 6, 0x40000000)
        ctx.fill(x, y, x + 84, y + 6, 0xFFE84C4C.toInt())
        ctx.drawString(font, "§cSlayer Boss §770%", x, y + 8, 0xFFFFFF, true)
    }

    private fun drawBorder(ctx: GuiGraphics, x: Int, y: Int, w: Int, h: Int, color: Int) {
        ctx.fill(x, y, x + w, y + 1, color)
        ctx.fill(x, y + h - 1, x + w, y + h, color)
        ctx.fill(x, y + 1, x + 1, y + h - 1, color)
        ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, color)
    }
}
