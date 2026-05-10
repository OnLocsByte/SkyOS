package dev.skyos.config

import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.LoggerFactory

object ConfigManager {
    private val logger = LoggerFactory.getLogger("SkyOS/Config")
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val configFile get() = FabricLoader.getInstance().configDir.resolve("skyos.json").toFile()

    var config: SkyOsConfig = SkyOsConfig()

    fun load() {
        if (!configFile.exists()) {
            save()
            return
        }
        runCatching {
            config = gson.fromJson(configFile.readText(), SkyOsConfig::class.java) ?: SkyOsConfig()
        }.onFailure {
            logger.error("Failed to load SkyOS config, using defaults", it)
            config = SkyOsConfig()
        }
    }

    fun save() {
        runCatching {
            configFile.writeText(gson.toJson(config))
        }.onFailure {
            logger.error("Failed to save SkyOS config", it)
        }
    }
}
