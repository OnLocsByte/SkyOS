package dev.skyos.features.general

import dev.skyos.config.ConfigManager
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents

object FirmamentAnnouncerRemover {

    private val FIRMAMENT_PATTERNS = listOf(
        Regex("""^\[Firmament\]"""),
        Regex("""^\[FMT\]"""),
        Regex("""Firmament.*update.*available""", RegexOption.IGNORE_CASE),
        Regex("""Firmament.*announcement""", RegexOption.IGNORE_CASE)
    )

    fun init() {
        // In MC 1.21.11 the event is ALLOW_GAME (was ALLOW_GAME_MESSAGE in older versions)
        ClientReceiveMessageEvents.ALLOW_GAME.register { message, _ ->
            if (!ConfigManager.config.general.firmamentAnnouncerRemoverEnabled) return@register true
            val raw = message.string
            FIRMAMENT_PATTERNS.none { it.containsMatchIn(raw) }
        }
    }
}
