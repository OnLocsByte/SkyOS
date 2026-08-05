package dev.freelocs.aetherion.features

/**
 * A single sub-option shown under a feature in the config GUI.
 * Currently only boolean toggles are needed by the built-in features;
 * new option kinds can be added as additional sealed subclasses.
 */
sealed class ConfigOption(val label: String, val description: String) {
    class Toggle(
        label: String,
        description: String,
        val getter: () -> Boolean,
        val setter: (Boolean) -> Unit,
    ) : ConfigOption(label, description)
}

/**
 * A single, independently toggleable mod feature. Each feature owns its
 * event registration and reads/writes its own slice of [dev.freelocs.aetherion.config.AetherionConfig].
 */
interface AetherionFeature {
    val id: String
    val displayName: String
    val description: String
    val category: String

    var enabled: Boolean

    /** Additional per-feature toggles shown in the config GUI below the main switch. */
    fun subOptions(): List<ConfigOption> = emptyList()

    /** Registers Fabric API event listeners. Called once during mod init, regardless of [enabled]. */
    fun init() {}
}
