package dev.skyos.features.general

import org.slf4j.LoggerFactory

object ModHider {
    private val logger = LoggerFactory.getLogger("SkyOS/ModHider")

    val DEFAULT_HIDDEN_CHANNELS: Set<String> = setOf()

    fun init() {
        logger.debug("ModHider initialized — always active")
    }

    fun isEnabled(): Boolean = true

    fun shouldHideChannel(channelId: String): Boolean =
        channelId in DEFAULT_HIDDEN_CHANNELS
}
