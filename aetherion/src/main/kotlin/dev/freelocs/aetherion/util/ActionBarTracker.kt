package dev.freelocs.aetherion.util

/**
 * Holds the most recently received action bar text.
 *
 * Populated by [dev.freelocs.aetherion.mixin.ActionBarAccessorMixin], which only
 * *reads* the text Minecraft is about to display — it never cancels, delays or
 * rewrites the message. This is the same client-side data every skill/slayer/
 * dungeon overlay is derived from; nothing here touches network traffic.
 */
object ActionBarTracker {

    @Volatile
    var lastMessage: String = ""
        private set

    @Volatile
    var lastUpdateMs: Long = 0L
        private set

    fun onActionBarMessage(text: String) {
        lastMessage = text
        lastUpdateMs = System.currentTimeMillis()
    }

    fun isStale(maxAgeMs: Long = 3000L): Boolean =
        System.currentTimeMillis() - lastUpdateMs > maxAgeMs
}
