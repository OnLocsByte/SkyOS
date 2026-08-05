package dev.freelocs.aetherion.config

data class DungeonTimerConfig(
    var enabled: Boolean = true,
    var showTotalTimer: Boolean = true,
    var showPhaseSplits: Boolean = true,
    var position: HudPosition = HudPosition(0.02f, 0.30f)
)

data class SkillProgressConfig(
    var enabled: Boolean = true,
    var showFarming: Boolean = true,
    var showMining: Boolean = true,
    var showForaging: Boolean = true,
    var showFishing: Boolean = true,
    var showCombat: Boolean = true,
    var showEnchanting: Boolean = true,
    var showAlchemy: Boolean = true,
    var showTaming: Boolean = true,
    var hideAfterSeconds: Int = 5,
    var position: HudPosition = HudPosition(0.02f, 0.45f)
)

data class TooltipConfig(
    var enabled: Boolean = true,
    var rarityColorDivider: Boolean = true,
    var showItemId: Boolean = false
)

data class SlayerHpConfig(
    var enabled: Boolean = true,
    var hideVanillaActionBarText: Boolean = true,
    var showPercentage: Boolean = true,
    var position: HudPosition = HudPosition(0.5f, 0.85f)
)

data class BestFlipConfig(
    var enabled: Boolean = false,
    var apiKey: String = "",
    var minProfitPercent: Int = 8,
    var maxResults: Int = 5,
    var refreshIntervalSeconds: Int = 60,
    var includeAuctionHouseHeuristic: Boolean = false,
    var position: HudPosition = HudPosition(0.75f, 0.02f)
)

data class AetherionConfig(
    var dungeonTimer: DungeonTimerConfig = DungeonTimerConfig(),
    var skillProgress: SkillProgressConfig = SkillProgressConfig(),
    var tooltip: TooltipConfig = TooltipConfig(),
    var slayerHp: SlayerHpConfig = SlayerHpConfig(),
    var bestFlip: BestFlipConfig = BestFlipConfig()
)
