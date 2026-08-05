package dev.freelocs.aetherion.feature.skills

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
 * Parses Hypixel Skyblock's action-bar skill XP segment, e.g. "+13 Farming (532,204.1/600,000)",
 * into a small progress bar with an optional ETA to the next level.
 *
 * If Hypixel changes the action-bar wording, adjust [XP_GAIN_PATTERN] — everything else
 * (HUD, config, ETA math) keeps working unchanged.
 */
object SkillProgressFeature : Feature, HudElement {

    override val id = "skill_progress"
    override val displayName = "Skill Progress"

    private val SKILL_NAMES = listOf(
        "Farming", "Mining", "Combat", "Foraging", "Fishing",
        "Enchanting", "Alchemy", "Taming", "Carpentry", "Runecrafting", "Social"
    )
    private val XP_GAIN_PATTERN = Regex(
        """\+([\d,.]+) (${SKILL_NAMES.joinToString("|")}) \(([\d,.]+) */ *([\d,.]+)\)"""
    )

    private data class Sample(val gain: Double, val atMs: Long)

    private var skillName: String? = null
    private var current: Double = 0.0
    private var max: Double = 1.0
    private var lastGain: Double = 0.0
    private var lastSeenMs: Long = 0L
    private val recentSamples = ArrayDeque<Sample>()

    override fun isEnabled(): Boolean = ConfigManager.config.skillProgress.enabled

    override fun init() {
        HudManager.register(this)
        var lastParsedActionBarMs = 0L

        ClientTickEvents.END_CLIENT_TICK.register {
            if (!isEnabled()) return@register
            if (ActionBarTracker.lastUpdateMs == lastParsedActionBarMs) return@register
            lastParsedActionBarMs = ActionBarTracker.lastUpdateMs

            val match = XP_GAIN_PATTERN.find(ActionBarTracker.lastMessage) ?: return@register
            val gain = match.groupValues[1].replace(",", "").toDoubleOrNull() ?: return@register
            val cur = match.groupValues[3].replace(",", "").toDoubleOrNull() ?: return@register
            val mx = match.groupValues[4].replace(",", "").toDoubleOrNull() ?: return@register

            skillName = match.groupValues[2]
            current = cur
            max = mx
            lastGain = gain
            lastSeenMs = System.currentTimeMillis()

            recentSamples.addLast(Sample(gain, lastSeenMs))
            while (recentSamples.isNotEmpty() && lastSeenMs - recentSamples.first().atMs > 60_000) {
                recentSamples.removeFirst()
            }
        }
    }

    private fun etaSeconds(): Long? {
        if (recentSamples.size < 2) return null
        val windowMs = (recentSamples.last().atMs - recentSamples.first().atMs).coerceAtLeast(1)
        val gainPerMs = recentSamples.sumOf { it.gain } / windowMs
        if (gainPerMs <= 0.0) return null
        val remaining = (max - current).coerceAtLeast(0.0)
        return (remaining / gainPerMs / 1000).toLong()
    }

    // ── HudElement ────────────────────────────────────────────────────────────

    override fun getPosition(): HudPosition = ConfigManager.config.skillProgress.position
    override fun savePosition() = ConfigManager.save()
    override fun previewSize(): Pair<Int, Int> = 100 to 26

    override fun render(ctx: GuiGraphics) {
        val name = skillName ?: return
        val cfg = ConfigManager.config.skillProgress
        if (System.currentTimeMillis() - lastSeenMs > cfg.hideAfterSeconds * 1000L) return

        val pos = getPosition()
        val font = Minecraft.getInstance().font
        val barW = 100
        val barH = 4

        ctx.drawString(font, "§b$name", pos.x, pos.y, 0xFFFFFF, true)
        if (cfg.showActionGain) {
            ctx.drawString(font, "§a+${trimNum(lastGain)}", pos.x + font.width(name) + 14, pos.y, 0xFFFFFF, true)
        }

        val barY = pos.y + 10
        val pct = (current / max).coerceIn(0.0, 1.0)
        ctx.fill(pos.x, barY, pos.x + barW, barY + barH, 0x40FFFFFF)
        ctx.fill(pos.x, barY, pos.x + (barW * pct).toInt(), barY + barH, 0xFF3E9BFF.toInt())

        var y2 = barY + barH + 2
        ctx.drawString(font, "§7${trimNum(current)}§8/§7${trimNum(max)} §8(${"%.1f".format(pct * 100)}%)", pos.x, y2, 0xFFFFFF, true)

        if (cfg.showEtaToLevel) {
            val eta = etaSeconds()
            if (eta != null) {
                y2 += 10
                ctx.drawString(font, "§7ETA §f${formatDuration(eta)}", pos.x, y2, 0xFFFFFF, true)
            }
        }
    }

    override fun renderPreview(ctx: GuiGraphics, x: Int, y: Int) {
        val font = Minecraft.getInstance().font
        ctx.drawString(font, "§bFarming §a+13.2", x, y, 0xFFFFFF, true)
        ctx.fill(x, y + 10, x + 100, y + 14, 0x40FFFFFF)
        ctx.fill(x, y + 10, x + 60, y + 14, 0xFF3E9BFF.toInt())
    }

    private fun trimNum(v: Double): String =
        if (v == v.toLong().toDouble()) v.toLong().toString() else "%.1f".format(v)

    private fun formatDuration(totalSeconds: Long): String {
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        return if (h > 0) "%dh%02dm".format(h, m) else "%dm%02ds".format(m, s)
    }
}
