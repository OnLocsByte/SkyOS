package dev.freelocs.aetherion

import dev.freelocs.aetherion.command.AetherionCommand
import dev.freelocs.aetherion.config.ConfigManager
import dev.freelocs.aetherion.features.FeatureRegistry
import dev.freelocs.aetherion.features.bazaar.BestFlipFeature
import dev.freelocs.aetherion.features.dungeon.DungeonTimerFeature
import dev.freelocs.aetherion.features.items.ItemRarityTooltipFeature
import dev.freelocs.aetherion.features.skills.SkillXpOverlayFeature
import dev.freelocs.aetherion.features.slayer.SlayerHealthBarFeature
import dev.freelocs.aetherion.hud.HudManager
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import org.slf4j.LoggerFactory

object AetherionMod : ClientModInitializer {
    const val MOD_ID = "aetherion"
    const val VERSION = "0.1.0"

    private val logger = LoggerFactory.getLogger("Aetherion")

    override fun onInitializeClient() {
        ConfigManager.load()

        FeatureRegistry.register(DungeonTimerFeature)
        FeatureRegistry.register(SkillXpOverlayFeature)
        FeatureRegistry.register(ItemRarityTooltipFeature)
        FeatureRegistry.register(SlayerHealthBarFeature)
        FeatureRegistry.register(BestFlipFeature)
        FeatureRegistry.initAll()

        HudManager.init()
        AetherionCommand.register()

        ClientLifecycleEvents.CLIENT_STOPPING.register {
            ConfigManager.save()
        }

        logger.info("Aetherion $VERSION initialized — open config with /aetherion")
    }
}
