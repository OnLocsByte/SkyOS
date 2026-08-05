package dev.freelocs.aetherion.features.skills

import dev.freelocs.aetherion.config.ConfigManager
import dev.freelocs.aetherion.features.AetherionFeature
import dev.freelocs.aetherion.features.ConfigOption
import dev.freelocs.aetherion.gui.AetherionColors
import dev.freelocs.aetherion.hud.HudElement
import dev.freelocs.aetherion.hud.HudManager
import dev.freelocs.aetherion.util.SkillXpPatterns
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import java.util.Locale

private class SkillSession {
    var totalGained: Double = 0.0
    val startMillis: Long = System.currentTimeMillis()
}

/**
 * Tracks Skyblock's "+X <Skill>" action-bar XP messages per skill and
 * shows a session summary overlay (total gained, gain/hour) instead of
 * having to watch the action bar tick by.
 */
object SkillXpOverlayFeature : AetherionFeature {
    override val id = "skill_xp_overlay"
    override val displayName = "Skill XP Overlay"
    override val description = "Tracks XP gained per action and shows a rate overlay."
    override val category = "Skills"

    override var enabled: Boolean
        get() = ConfigManager.config.skillXpOverlay.enabled
        set(value) {
            ConfigManager.config.skillXpOverlay.enabled = value
            ConfigManager.save()
        }

    override fun subOptions(): List<ConfigOption> {
        val cfg = ConfigManager.config.skillXpOverlay
        return listOf(
            ConfigOption.Toggle(
                "Show rate per hour",
                "Show an estimated XP/hour based on this session",
                { cfg.showRatePerHour },
                { cfg.showRatePerHour = it; ConfigManager.save() }
            ),
            ConfigOption.Toggle(
                "Reset rate on skill switch",
                "Restart the session average whenever you switch skills",
                { cfg.resetRateOnSkillSwitch },
                { cfg.resetRateOnSkillSwitch = it; ConfigManager.save() }
            ),
        )
    }

    private val sessions = mutableMapOf<String, SkillSession>()
    @Volatile private var lastSkill: String? = null
    @Volatile private var lastGainAmount: Double = 0.0
    @Volatile private var lastGainMillis: Long = 0

    override fun init() {
        ClientReceiveMessageEvents.GAME.register { message, overlay ->
            if (!overlay || !enabled) return@register
            val match = SkillXpPatterns.XP_GAIN_PATTERN.find(message.string) ?: return@register
            val amount = SkillXpPatterns.parseAmount(match.groupValues[1])
            val skill = match.groupValues[2]

            val cfg = ConfigManager.config.skillXpOverlay
            if (cfg.resetRateOnSkillSwitch && lastSkill != null && lastSkill != skill) {
                sessions.remove(lastSkill)
            }

            sessions.getOrPut(skill) { SkillSession() }.totalGained += amount
            lastSkill = skill
            lastGainAmount = amount
            lastGainMillis = System.currentTimeMillis()
        }

        HudManager.register(SkillXpOverlayHud)
    }

    private fun ratePerHour(skill: String): Double {
        val session = sessions[skill] ?: return 0.0
        val elapsedHours = (System.currentTimeMillis() - session.startMillis) / 3_600_000.0
        if (elapsedHours < 1.0 / 3600.0) return 0.0
        return session.totalGained / elapsedHours
    }

    private object SkillXpOverlayHud : HudElement {
        override val id = "skill_xp_overlay"
        override val label = "Skill XP Overlay"

        override fun isFeatureEnabled(): Boolean = enabled

        override fun hasLiveData(): Boolean {
            val skill = lastSkill ?: return false
            val visibleMillis = ConfigManager.config.skillXpOverlay.overlayDurationSeconds * 1000L
            return System.currentTimeMillis() - lastGainMillis < visibleMillis && sessions.containsKey(skill)
        }

        override fun contentSize(): Pair<Int, Int> = 140 to 24

        override fun render(context: GuiGraphics, x: Int, y: Int) {
            val skill = lastSkill ?: "Farming"
            val cfg = ConfigManager.config.skillXpOverlay
            val font = Minecraft.getInstance().font
            val session = sessions[skill]
            val total = session?.totalGained ?: 0.0

            context.drawString(font, "$skill XP", x, y, AetherionColors.BRAND_TEAL, true)
            context.drawString(font, "+${format(total)}", x, y + 12, AetherionColors.FG_PRIMARY, true)
            if (cfg.showRatePerHour) {
                val rate = ratePerHour(skill)
                context.drawString(font, "${format(rate)}/h", x + 70, y + 12, AetherionColors.FG_MUTED, true)
            }
        }

        private fun format(value: Double): String = "%,.0f".format(Locale.US, value)
    }
}
