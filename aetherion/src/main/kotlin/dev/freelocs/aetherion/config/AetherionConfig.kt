package dev.freelocs.aetherion.config

/**
 * Persisted per-feature settings. Every feature gets its own data class so
 * new sub-options can be added without touching unrelated features.
 */
data class DungeonTimerConfig(
    var enabled: Boolean = true,
    var showPhaseSplits: Boolean = true,
    var showTotalTime: Boolean = true,
)

data class SkillXpOverlayConfig(
    var enabled: Boolean = true,
    var showRatePerHour: Boolean = true,
    var resetRateOnSkillSwitch: Boolean = false,
    var overlayDurationSeconds: Int = 5,
)

data class ItemRarityTooltipConfig(
    var enabled: Boolean = true,
    var colorItemName: Boolean = true,
    var showRarityBorder: Boolean = true,
)

data class SlayerHealthBarConfig(
    var enabled: Boolean = true,
    var showPercentage: Boolean = true,
    var showBossName: Boolean = true,
)

data class BestFlipConfig(
    var enabled: Boolean = false,
    var minProfitPerFlip: Long = 100_000,
    var minProfitMargin: Double = 0.10,
    var refreshIntervalSeconds: Int = 60,
    var maxResultsShown: Int = 5,
)

/**
 * Free-floating position of a HUD element, stored as a fraction of the
 * screen size so it stays valid across different resolutions.
 */
data class HudPositionConfig(
    var xFraction: Float = 0.02f,
    var yFraction: Float = 0.02f,
    var scale: Float = 1.0f,
)

data class AetherionConfig(
    var dungeonTimer: DungeonTimerConfig = DungeonTimerConfig(),
    var skillXpOverlay: SkillXpOverlayConfig = SkillXpOverlayConfig(),
    var itemRarityTooltip: ItemRarityTooltipConfig = ItemRarityTooltipConfig(),
    var slayerHealthBar: SlayerHealthBarConfig = SlayerHealthBarConfig(),
    var bestFlip: BestFlipConfig = BestFlipConfig(),
    var hudPositions: MutableMap<String, HudPositionConfig> = mutableMapOf(),
)
