package dev.freelocs.aetherion.feature.dungeon

import dev.freelocs.aetherion.config.ConfigManager
import dev.freelocs.aetherion.config.HudPosition
import dev.freelocs.aetherion.feature.Feature
import dev.freelocs.aetherion.feature.hud.HudElement
import dev.freelocs.aetherion.feature.hud.HudManager
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics

/**
 * Tracks elapsed time (and, best-effort, phase/boss splits) for a Hypixel Skyblock
 * Catacombs run, purely by reading chat messages — nothing is sent to the server.
 *
 * The trigger phrases below match Hypixel's commonly documented dungeon chat lines.
 * Hypixel occasionally rewords these; if splits stop triggering, adjust the regexes
 * below — everything else (timer, HUD, config) keeps working unchanged.
 */
object DungeonTimerFeature : Feature, HudElement {

    override val id = "dungeon_timer"
    override val displayName = "Dungeon Timer"

    private val START_TRIGGERS = listOf(
        Regex("""The dungeon timer has started!?""", RegexOption.IGNORE_CASE)
    )
    private val SPLIT_TRIGGERS = listOf(
        Regex("""^\[BOSS] *(.+?):"""),
        Regex("""Party > .*(entered|found).*secret""", RegexOption.IGNORE_CASE)
    )
    private val CLEAR_TRIGGER = Regex("""(Dungeon Cleared!|You have completed The Catacombs)""", RegexOption.IGNORE_CASE)
    private val DEATH_TRIGGER = Regex("""^\s*☠ .+ (died|has died)""")

    private var startMs: Long? = null
    private var finishedMs: Long? = null
    private val splits = mutableListOf<Pair<String, Long>>()
    private var deaths = 0

    override fun isEnabled(): Boolean = ConfigManager.config.dungeonTimer.enabled

    override fun init() {
        HudManager.register(this)

        ClientReceiveMessageEvents.GAME.register { message, _ ->
            val text = message.string

            if (START_TRIGGERS.any { it.containsMatchIn(text) }) {
                reset()
                startMs = System.currentTimeMillis()
            }

            if (startMs != null && finishedMs == null) {
                val split = SPLIT_TRIGGERS.firstNotNullOfOrNull { r -> r.find(text) }
                if (split != null) {
                    recordSplit(split.groupValues.getOrElse(1) { "Split" }.trim().ifBlank { "Split" })
                }
                if (DEATH_TRIGGER.containsMatchIn(text)) deaths++
                if (CLEAR_TRIGGER.containsMatchIn(text)) finishedMs = System.currentTimeMillis()
            }
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> reset() }
    }

    private fun reset() {
        startMs = null
        finishedMs = null
        splits.clear()
        deaths = 0
    }

    private fun recordSplit(label: String) {
        val start = startMs ?: return
        if (splits.size >= 12) return // cap growth on a noisy/long run
        splits.add(label to (System.currentTimeMillis() - start))
    }

    private fun elapsedMs(): Long {
        val start = startMs ?: return 0L
        return (finishedMs ?: System.currentTimeMillis()) - start
    }

    private fun format(ms: Long): String {
        val totalSeconds = ms / 1000
        return "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
    }

    // ── HudElement ────────────────────────────────────────────────────────────

    override fun getPosition(): HudPosition = ConfigManager.config.dungeonTimer.position
    override fun savePosition() = ConfigManager.save()
    override fun previewSize(): Pair<Int, Int> = 90 to 40

    override fun render(ctx: GuiGraphics) {
        if (startMs == null) return
        val cfg = ConfigManager.config.dungeonTimer
        val pos = getPosition()
        val font = Minecraft.getInstance().font

        ctx.drawString(font, "§bDungeon §7» §f${format(elapsedMs())}", pos.x, pos.y, 0xFFFFFF, true)
        var y = pos.y + 10
        if (cfg.showDeaths && deaths > 0) {
            ctx.drawString(font, "§c☠ $deaths", pos.x, y, 0xFFFFFF, true)
            y += 10
        }
        if (cfg.showPhaseSplits) {
            splits.takeLast(3).forEach { (label, ms) ->
                ctx.drawString(font, "§7$label §8${format(ms)}", pos.x, y, 0xFFFFFF, true)
                y += 10
            }
        }
    }

    override fun renderPreview(ctx: GuiGraphics, x: Int, y: Int) {
        ctx.drawString(Minecraft.getInstance().font, "§bDungeon §7» §f04:12", x, y, 0xFFFFFF, true)
    }
}
