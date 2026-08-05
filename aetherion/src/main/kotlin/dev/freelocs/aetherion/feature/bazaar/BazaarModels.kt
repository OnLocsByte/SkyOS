package dev.freelocs.aetherion.feature.bazaar

/** A single display-only flip opportunity. Aetherion never acts on this automatically. */
data class FlipResult(
    val itemId: String,
    val source: Source,
    val buyPrice: Double,
    val sellPrice: Double,
    val marginPerUnit: Double
) {
    enum class Source { BAZAAR, AUCTION_HOUSE }
}
