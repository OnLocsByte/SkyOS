package dev.skyos

import dev.skyos.command.SkyOsCommand
import dev.skyos.config.ConfigManager
import dev.skyos.features.general.AutoModUpdater
import dev.skyos.features.general.FirmamentAnnouncerRemover
import dev.skyos.features.general.ModHider
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import org.slf4j.LoggerFactory

object SkyOsMod : ClientModInitializer {
    const val MOD_ID = "skyos"
    const val VERSION = "0.1.0"

    private val logger = LoggerFactory.getLogger("SkyOS")

    override fun onInitializeClient() {
        ConfigManager.load()

        SkyOsCommand.register()
        AutoModUpdater.init()
        FirmamentAnnouncerRemover.init()
        ModHider.init()

        ClientLifecycleEvents.CLIENT_STOPPING.register {
            ConfigManager.save()
        }

        logger.info("SkyOS $VERSION initialized — open config with /skyos")
    }
}
