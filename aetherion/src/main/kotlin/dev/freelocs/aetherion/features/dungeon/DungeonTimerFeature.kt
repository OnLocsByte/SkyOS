package dev.freelocs.aetherion.features.dungeon

import dev.freelocs.aetherion.config.ConfigManager
import dev.freelocs.aetherion.features.AetherionFeature
import dev.freelocs.aetherion.features.ConfigOption
import dev.freelocs.aetherion.gui.AetherionColors
import dev.freelocs.aetherion.hud.HudElement
import dev.freelocs.aetherion.hud.HudManager
import dev.freelocs.aetherion.util.DungeonPatterns
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.scores.DisplaySlot

private data class Split(val label: String, val elapsedSeconds: Int)

/**
 * Mirrors the Catacombs run's "Time Elapsed" scoreboard line as a clean
 * floating HUD timer, and records a split whenever a boss-phase chat
 * announcement fires so you can see how long each phase took at a glance.
 *
 * The scoreboard line is read verbatim from Hypixel's own sidebar (the
 * same source the game already renders), so the timer itself is reliable.
 * Boss-phase splits rely on chat text patterns that can shift between
 * Hypixel updates — if a boss message doesn't match, the timer keeps
 * running fine, it just won't record that split.
 */
object DungeonTimerFeature : AetherionFeature {
    override val id = "dungeon_timer"
    override val displayName = "Dungeon Timer"
    override val description = "Floating run timer with per-boss phase splits."
    override val category = "Dungeons"

    private val TIME_ELAPSED_LINE = Regex("""Time Elapsed:\s*(?:(\d+)m)?\s*(\d+)s""", RegexOption.IGNORE_CASE)
    private const val MAX_SPLITS = 6

    override var enabled: Boolean
        get() = ConfigManager.config.dungeonTimer.enabled
        set(value) {
            ConfigManager.config.dungeonTimer.enabled = value
            ConfigManager.save()
        }

    override fun subOptions(): List<ConfigOption> {
        val cfg = ConfigManager.config.dungeonTimer
        return listOf(
            ConfigOption.Toggle(
                "Show total time",
                "Show the current run's elapsed time",
                { cfg.showTotalTime },
                { cfg.showTotalTime = it; ConfigManager.save() }
            ),
            ConfigOption.Toggle(
                "Show phase splits",
                "Record a split whenever a boss phase begins",
                { cfg.showPhaseSplits },
                { cfg.showPhaseSplits = it; ConfigManager.save() }
            ),
        )
    }

    @Volatile private var inDungeon = false
    @Volatile private var elapsedSeconds = 0
    private val splits = mutableListOf<Split>()

    override fun init() {
        ClientTickEvents.END_CLIENT_TICK.register { client -> tick(client) }

        ClientReceiveMessageEvents.GAME.register { message, overlay ->
            if (overlay || !enabled || !inDungeon) return@register
            val text = message.string
            if (DungeonPatterns.BOSS_ENTER_PATTERN.containsMatchIn(text)) {
                val name = DungeonPatterns.BOSS_ENTER_PATTERN.find(text)?.groupValues?.get(1) ?: "Boss"
                recordSplit(name)
            } else if (DungeonPatterns.RUN_CLEARED.containsMatchIn(text) || DungeonPatterns.RUN_FAILED.containsMatchIn(text)) {
                recordSplit(if (DungeonPatterns.RUN_CLEARED.containsMatchIn(text)) "Cleared" else "Failed")
            }
        }

        HudManager.register(DungeonTimerHud)
    }

    private fun tick(client: Minecraft) {
        if (!enabled) return
        val level = client.level
        val scoreboard = level?.scoreboard
        val objective = scoreboard?.getDisplayObjective(DisplaySlot.SIDEBAR)
        if (objective == null) {
            if (inDungeon) resetRun()
            return
        }

        val lines = scoreboard.listPlayerScores(objective)
            .sortedByDescending { it.value() }
            .mapNotNull { it.display()?.string }

        val timeLine = lines.firstOrNull { TIME_ELAPSED_LINE.containsMatchIn(it) }
        if (timeLine == null) {
            if (inDungeon) resetRun()
            return
        }

        val match = TIME_ELAPSED_LINE.find(timeLine)!!
        val minutes = match.groupValues[1].toIntOrNull() ?: 0
        val seconds = match.groupValues[2].toIntOrNull() ?: 0
        elapsedSeconds = minutes * 60 + seconds
        inDungeon = true
    }

    private fun recordSplit(label: String) {
        splits += Split(label, elapsedSeconds)
        while (splits.size > MAX_SPLITS) splits.removeAt(0)
    }

    private fun resetRun() {
        inDungeon = false
        elapsedSeconds = 0
        splits.clear()
    }

    private fun formatTime(totalSeconds: Int): String {
        val m = totalSeconds / 60
        val s = totalSeconds % 60
        return "%02d:%02d".format(m, s)
    }

    private object DungeonTimerHud : HudElement {
        override val id = "dungeon_timer"
        override val label = "Dungeon Timer"

        override fun isFeatureEnabled(): Boolean = enabled
        override fun hasLiveData(): Boolean = inDungeon

        override fun contentSize(): Pair<Int, Int> {
            val cfg = ConfigManager.config.dungeonTimer
            val splitLines = if (cfg.showPhaseSplits) splits.size else 0
            return 120 to (12 + splitLines * 10)
        }

        override fun render(context: GuiGraphics, x: Int, y: Int) {
            val cfg = ConfigManager.config.dungeonTimer
            val font = Minecraft.getInstance().font
            var lineY = y

            if (cfg.showTotalTime) {
                context.drawString(font, "⏱ ${formatTime(elapsedSeconds)}", x, lineY, AetherionColors.BRAND_CYAN, true)
                lineY += 12
            }
            if (cfg.showPhaseSplits) {
                for (split in splits) {
                    val text = "${split.label}  ${formatTime(split.elapsedSeconds)}"
                    context.drawString(font, text, x + 4, lineY, AetherionColors.FG_SECONDARY, true)
                    lineY += 10
                }
            }
        }
    }
}
