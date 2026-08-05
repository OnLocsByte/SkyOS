package dev.freelocs.aetherion.util

import net.minecraft.client.Minecraft
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.PlayerTeam

/**
 * Reads the current sidebar scoreboard as plain display strings.
 *
 * Hypixel (like most large server networks) renders each sidebar line as a
 * fake score-holder whose actual text lives in a [PlayerTeam] prefix/suffix
 * pair rather than the holder name itself, so we resolve that team formatting
 * instead of relying on the raw holder string. This is best-effort: if
 * Hypixel changes its sidebar encoding, lines simply come back empty rather
 * than throwing.
 */
object ScoreboardUtil {

    fun sidebarLines(): List<String> = runCatching {
        val mc = Minecraft.getInstance()
        val scoreboard = mc.level?.scoreboard ?: return@runCatching emptyList()
        val objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) ?: return@runCatching emptyList()

        scoreboard.listPlayerScores(objective)
            .filter { !it.isHidden }
            .sortedByDescending { it.value() }
            .map { entry ->
                val holderName = entry.owner()
                val team: PlayerTeam? = scoreboard.getPlayersTeam(holderName)
                if (team != null) {
                    (team.playerPrefix.string + holderName + team.playerSuffix.string).trim()
                } else {
                    holderName.trim()
                }
            }
            .filter { it.isNotBlank() }
    }.getOrDefault(emptyList())
}
