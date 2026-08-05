package dev.freelocs.aetherion.features.skills

import dev.freelocs.aetherion.config.ConfigManager
import dev.freelocs.aetherion.gui.AetherionColors
import dev.freelocs.aetherion.hud.HudElement
import dev.freelocs.aetherion.hud.HudOverlayManager
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics

/**
 * Parses Hypixel's per-action skill XP action-bar text (e.g. "+12 Farming
 * (10,532/12,000)") into a small HUD overlay showing recent XP/hour and
 * progress to the next level per skill.
 */
object SkillProgressFeature {

    private val XP_GAIN_REGEX = Regex(
        """\+([\d,.]+) (Farming|Mining|Foraging|Fishing|Combat|Enchanting|Alchemy|Taming)\s*\(([\d,.]+)\s*/\s*([\d,.]+)\)"""
    )

    private data class SkillState(
        var current: Double = 0.0,
        var needed: Double = 0.0,
        var sessionGained: Double = 0.0,
        var lastUpdateMs: Long = 0L
    )

    private val skills = linkedMapOf<String, SkillState>()

    fun init() {
        ClientReceiveMessageEvents.ALLOW_GAME.register { message, overlay ->
            if (overlay && ConfigManager.config.skillProgress.enabled) {
                handleActionBar(message.string)
            }
            true
        }

        ClientTickEvents.END_CLIENT_TICK.register {
            val cfg = ConfigManager.config.skillProgress
            val cutoffMs = System.currentTimeMillis() - cfg.hideAfterSeconds * 1000L
            skills.entries.removeAll { it.value.lastUpdateMs < cutoffMs }
        }

        HudOverlayManager.register(
            HudElement(
                id = "skill_progress",
                displayName = "Skill XP Progress",
                previewWidth = 150,
                previewHeight = 60,
                isEnabled = { ConfigManager.config.skillProgress.enabled && skills.isNotEmpty() },
                getPosition = { ConfigManager.config.skillProgress.position },
                setPosition = { ConfigManager.config.skillProgress.position = it },
                render = ::render
            )
        )
    }

    private fun handleActionBar(text: String) {
        val match = XP_GAIN_REGEX.find(text) ?: return
        val (amountStr, skillName, currentStr, neededStr) = match.destructured
        val amount = amountStr.replace(",", "").toDoubleOrNull() ?: return
        val current = currentStr.replace(",", "").toDoubleOrNull() ?: return
        val needed = neededStr.replace(",", "").toDoubleOrNull() ?: return

        if (!isSkillVisible(skillName)) return

        val state = skills.getOrPut(skillName) { SkillState() }
        state.current = current
        state.needed = needed
        state.sessionGained += amount
        state.lastUpdateMs = System.currentTimeMillis()
    }

    private fun isSkillVisible(skillName: String): Boolean {
        val cfg = ConfigManager.config.skillProgress
        return when (skillName) {
            "Farming" -> cfg.showFarming
            "Mining" -> cfg.showMining
            "Foraging" -> cfg.showForaging
            "Fishing" -> cfg.showFishing
            "Combat" -> cfg.showCombat
            "Enchanting" -> cfg.showEnchanting
            "Alchemy" -> cfg.showAlchemy
            "Taming" -> cfg.showTaming
            else -> true
        }
    }

    private fun render(ctx: GuiGraphics, x: Int, y: Int) {
        val font = Minecraft.getInstance().font
        var line = y
        for ((skill, state) in skills) {
            val progress = if (state.needed > 0) (state.current / state.needed).coerceIn(0.0, 1.0) else 0.0
            val text = "§b$skill §7» §a+%.0f §7(%.1f%%)".format(state.sessionGained, progress * 100)
            ctx.drawString(font, text, x, line, AetherionColors.FG_PRIMARY, true)
            drawProgressBar(ctx, x, line + 9, 90, progress)
            line += 16
        }
    }

    private fun drawProgressBar(ctx: GuiGraphics, x: Int, y: Int, width: Int, progress: Double) {
        ctx.fill(x, y, x + width, y + 2, AetherionColors.TOGGLE_OFF)
        val filled = (width * progress).toInt().coerceIn(0, width)
        if (filled > 0) ctx.fill(x, y, x + filled, y + 2, AetherionColors.BRAND_CYAN)
    }
}
