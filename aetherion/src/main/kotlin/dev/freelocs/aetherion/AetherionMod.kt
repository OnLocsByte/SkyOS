package dev.freelocs.aetherion

import dev.freelocs.aetherion.command.AetherionCommand
import dev.freelocs.aetherion.config.ConfigManager
import dev.freelocs.aetherion.features.bazaar.BestFlipFeature
import dev.freelocs.aetherion.features.dungeons.DungeonTimerFeature
import dev.freelocs.aetherion.features.skills.SkillProgressFeature
import dev.freelocs.aetherion.features.slayer.SlayerHealthBarFeature
import dev.freelocs.aetherion.features.tooltip.RarityTooltipFeature
import dev.freelocs.aetherion.hud.HudOverlayManager
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.Minecraft
import org.slf4j.LoggerFactory

/**
 * Aetherion — client-side information and quality-of-life overlays for
 * Hypixel Skyblock. Every feature only reads information the server already
 * sends (chat, action bar, scoreboard, tooltips) and renders it more
 * clearly; nothing here automates actions or hides mod presence from the
 * server.
 */
object AetherionMod : ClientModInitializer {
    const val MOD_ID = "aetherion"
    const val VERSION = "0.1.0"

    private val logger = LoggerFactory.getLogger("Aetherion")

    override fun onInitializeClient() {
        ConfigManager.load()

        AetherionCommand.register()

        DungeonTimerFeature.init()
        SkillProgressFeature.init()
        RarityTooltipFeature.init()
        SlayerHealthBarFeature.init()
        BestFlipFeature.init()

        HudRenderCallback.EVENT.register { ctx, _ ->
            val window = Minecraft.getInstance().window
            HudOverlayManager.renderEnabled(ctx, window.guiScaledWidth, window.guiScaledHeight)
        }

        ClientLifecycleEvents.CLIENT_STOPPING.register {
            ConfigManager.save()
        }

        logger.info("Aetherion $VERSION initialized — open the config with /aetherion")
    }
}
