package dev.freelocs.aetherion.util

/**
 * Chat/title triggers used to track a Catacombs run. These match the
 * publicly documented Hypixel dungeon message formats; exact wording can
 * shift between Hypixel updates and may need re-tuning against a live run.
 */
object DungeonPatterns {
    /** Title shown right as a dungeon run starts, after the 3-2-1 countdown. */
    val RUN_START_TITLE = Regex("""^(?:§.)*GO!(?:§.)*$""")

    /** Chat message on successful dungeon completion. */
    val RUN_CLEARED = Regex("""Dungeon Cleared!""")

    /** Chat message when a run fails (party wipe / time-out). */
    val RUN_FAILED = Regex("""(You have failed the dungeon|Your party failed)""", RegexOption.IGNORE_CASE)

    /** Boss-phase entry announcements, one per Catacombs floor's boss. */
    val BOSS_ENTER_PATTERN = Regex(
        """^\[BOSS] (Bonzo|Scarf|The Professor|Thorn|Livid|Sadan|Maxor|Storm|Goldor|Necron|Wither King)"""
    )
}
