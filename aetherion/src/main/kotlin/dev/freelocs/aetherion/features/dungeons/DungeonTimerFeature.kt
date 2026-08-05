package dev.freelocs.aetherion.features.dungeons

import dev.freelocs.aetherion.config.ConfigManager
import dev.freelocs.aetherion.gui.AetherionColors
import dev.freelocs.aetherion.hud.HudElement
import dev.freelocs.aetherion.hud.HudOverlayManager
import dev.freelocs.aetherion.util.ScoreboardUtil
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft

/**
 * Dungeon run timer with per-phase splits.
 *
 * Reads the "Time Elapsed:" and "Cleared:" lines Hypixel already writes into
 * the sidebar scoreboard during an active dungeon run — this is purely a
 * nicer re-display of information the server already sent, not derived
 * data. A new split is recorded whenever the clear percentage increases.
 */
object DungeonTimerFeature {

    private val TIME_ELAPSED_REGEX = Regex("""Time Elapsed:\s*(?:(\d+)h)?\s*(?:(\d+)m)?\s*(?:(\d+)s)?""")
    private val CLEARED_REGEX = Regex("""Cleared:\s*(\d+)%""")

    private data class Split(val percent: Int, val elapsedSeconds: Int)

    @Volatile private var inDungeon = false
    @Volatile private var elapsedSeconds = 0
    @Volatile private var lastClearedPercent = -1
    private val splits = mutableListOf<Split>()

    fun init() {
        ClientTickEvents.END_CLIENT_TICK.register {
            if (!ConfigManager.config.dungeonTimer.enabled) return@register
            if (Minecraft.getInstance().level == null) {
                reset()
                return@register
            }
            pollScoreboard()
        }

        HudOverlayManager.register(
            HudElement(
                id = "dungeon_timer",
                displayName = "Dungeon Timer",
                previewWidth = 130,
                previewHeight = 28,
                isEnabled = { ConfigManager.config.dungeonTimer.enabled && inDungeon },
                getPosition = { ConfigManager.config.dungeonTimer.position },
                setPosition = { ConfigManager.config.dungeonTimer.position = it },
                render = ::render
            )
        )
    }

    private fun pollScoreboard() {
        val lines = ScoreboardUtil.sidebarLines()
        val timeLine = lines.firstNotNullOfOrNull { TIME_ELAPSED_REGEX.find(it) }
        val clearedLine = lines.firstNotNullOfOrNull { CLEARED_REGEX.find(it) }

        if (timeLine == null) {
            reset()
            return
        }

        inDungeon = true
        val h = timeLine.groupValues[1].toIntOrNull() ?: 0
        val m = timeLine.groupValues[2].toIntOrNull() ?: 0
        val s = timeLine.groupValues[3].toIntOrNull() ?: 0
        elapsedSeconds = h * 3600 + m * 60 + s

        val percent = clearedLine?.groupValues?.get(1)?.toIntOrNull()
        if (percent != null && percent != lastClearedPercent) {
            splits.add(Split(percent, elapsedSeconds))
            lastClearedPercent = percent
        }
    }

    private fun reset() {
        if (!inDungeon) return
        inDungeon = false
        elapsedSeconds = 0
        lastClearedPercent = -1
        splits.clear()
    }

    private fun formatDuration(totalSeconds: Int): String {
        val m = totalSeconds / 60
        val s = totalSeconds % 60
        return "%d:%02d".format(m, s)
    }

    private fun render(ctx: net.minecraft.client.gui.GuiGraphics, x: Int, y: Int) {
        val mc = Minecraft.getInstance()
        val font = mc.font
        val cfg = ConfigManager.config.dungeonTimer

        var line = y
        if (cfg.showTotalTimer) {
            val text = "§bDungeon §7» §f${formatDuration(elapsedSeconds)}"
            ctx.drawString(font, text, x, line, AetherionColors.FG_PRIMARY, true)
            line += 10
        }
        if (cfg.showPhaseSplits && splits.isNotEmpty()) {
            val last = splits.last()
            val prev = splits.getOrNull(splits.size - 2)
            val splitDuration = last.elapsedSeconds - (prev?.elapsedSeconds ?: 0)
            val text = "§7${last.percent}% §8» §a${formatDuration(splitDuration)}"
            ctx.drawString(font, text, x, line, AetherionColors.FG_SECONDARY, true)
        }
    }
}
