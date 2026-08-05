package dev.freelocs.aetherion.feature

/**
 * A single, independently toggleable client-side feature module.
 *
 * Every feature owns its own config section and registers itself with
 * [FeatureManager]. Features must only read game/network state (chat events,
 * scoreboard, action bar) and render information — none of them are allowed
 * to send packets on the player's behalf or alter outgoing traffic.
 */
interface Feature {
    /** Stable id, used for logging and config lookups. */
    val id: String

    /** Human-readable name shown in the config GUI. */
    val displayName: String

    /** Whether the feature is currently switched on in the config. */
    fun isEnabled(): Boolean

    /** Called once during mod init to register event listeners. */
    fun init() {}
}
