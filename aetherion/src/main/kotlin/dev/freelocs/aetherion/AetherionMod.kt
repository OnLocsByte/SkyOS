package dev.freelocs.aetherion

import dev.freelocs.aetherion.command.AetherionCommand
import dev.freelocs.aetherion.config.ConfigManager
import dev.freelocs.aetherion.feature.FeatureManager
import dev.freelocs.aetherion.feature.bazaar.BestFlipFeature
import dev.freelocs.aetherion.feature.dungeon.DungeonTimerFeature
import dev.freelocs.aetherion.feature.hud.HudOverlayRenderer
import dev.freelocs.aetherion.feature.skills.SkillProgressFeature
import dev.freelocs.aetherion.feature.slayer.SlayerHealthBarFeature
import dev.freelocs.aetherion.feature.tooltip.ItemRarityTooltipFeature
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import org.slf4j.LoggerFactory

object AetherionMod : ClientModInitializer {
    const val MOD_ID = "aetherion"
    const val VERSION = "0.1.0"

    private val logger = LoggerFactory.getLogger("Aetherion")

    override fun onInitializeClient() {
        ConfigManager.load()

        AetherionCommand.register()
        HudOverlayRenderer.init()

        FeatureManager.register(DungeonTimerFeature)
        FeatureManager.register(SkillProgressFeature)
        FeatureManager.register(ItemRarityTooltipFeature)
        FeatureManager.register(SlayerHealthBarFeature)
        FeatureManager.register(BestFlipFeature)
        FeatureManager.initAll()

        ClientLifecycleEvents.CLIENT_STOPPING.register {
            ConfigManager.save()
        }

        logger.info("Aetherion $VERSION initialized — open config with /aetherion")
    }
}
