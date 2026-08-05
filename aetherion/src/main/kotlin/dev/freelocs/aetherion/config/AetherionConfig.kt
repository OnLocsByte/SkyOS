package dev.freelocs.aetherion.config

/** Screen position + scale of a draggable HUD element, editable in the HUD editor. */
data class HudPosition(
    var x: Int = 10,
    var y: Int = 10,
    var scale: Float = 1.0f
)

data class DungeonTimerConfig(
    var enabled: Boolean = true,
    var showPhaseSplits: Boolean = true,
    var showDeaths: Boolean = true,
    var position: HudPosition = HudPosition(10, 10)
)

data class SkillProgressConfig(
    var enabled: Boolean = true,
    var showActionGain: Boolean = true,
    var showEtaToLevel: Boolean = true,
    var hideAfterSeconds: Int = 5,
    var position: HudPosition = HudPosition(10, 40)
)

data class ItemRarityTooltipConfig(
    var enabled: Boolean = true,
    var coloredBorder: Boolean = true,
    var highlightReforge: Boolean = true
)

data class SlayerHealthBarConfig(
    var enabled: Boolean = true,
    var showPercentage: Boolean = true,
    var showAbsoluteHp: Boolean = false,
    var position: HudPosition = HudPosition(10, 70)
)

data class BestFlipConfig(
    var enabled: Boolean = false,
    var scanBazaar: Boolean = true,
    var scanAuctionHouse: Boolean = false,
    var minProfitPerUnit: Int = 50_000,
    var maxResults: Int = 10,
    var refreshIntervalSeconds: Int = 120,
    var showHudOverlay: Boolean = false,
    var hudPosition: HudPosition = HudPosition(10, 100)
)

data class HudEditorConfig(
    var showLabels: Boolean = true,
    var snapToGridPx: Int = 2
)

data class AetherionConfig(
    var dungeonTimer: DungeonTimerConfig = DungeonTimerConfig(),
    var skillProgress: SkillProgressConfig = SkillProgressConfig(),
    var itemRarityTooltip: ItemRarityTooltipConfig = ItemRarityTooltipConfig(),
    var slayerHealthBar: SlayerHealthBarConfig = SlayerHealthBarConfig(),
    var bestFlip: BestFlipConfig = BestFlipConfig(),
    var hudEditor: HudEditorConfig = HudEditorConfig()
)
