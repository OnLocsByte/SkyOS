package dev.freelocs.aetherion.feature.bazaar

import com.google.gson.JsonParser
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration

/**
 * Reads Hypixel's public Skyblock game-data endpoints. Both the Bazaar and Auctions
 * endpoints are public data (not player-specific) and do not require a Hypixel API key —
 * only per-player endpoints like /skyblock/profile do. This client is read-only: it never
 * places bids, buy orders, or purchases.
 */
object HypixelApiClient {
    private val logger = LoggerFactory.getLogger("Aetherion/HypixelApi")
    private const val BAZAAR_URL = "https://api.hypixel.net/v2/skyblock/bazaar"
    private const val AUCTIONS_URL = "https://api.hypixel.net/v2/skyblock/auctions"

    private val client: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    fun fetchBazaar(): List<FlipResult> {
        val body = get(BAZAAR_URL) ?: return emptyList()
        val root = runCatching { JsonParser.parseString(body).asJsonObject }.getOrNull() ?: return emptyList()
        val products = root.getAsJsonObject("products") ?: return emptyList()

        val results = mutableListOf<FlipResult>()
        for ((itemId, el) in products.entrySet()) {
            val product = runCatching { el.asJsonObject }.getOrNull() ?: continue
            val quickStatus = product.getAsJsonObject("quick_status") ?: continue
            val buy = quickStatus.get("buyPrice")?.asDouble ?: continue
            val sell = quickStatus.get("sellPrice")?.asDouble ?: continue
            if (buy <= 0 || sell <= 0) continue
            val margin = buy - sell
            if (margin <= 0) continue
            results.add(FlipResult(itemId, FlipResult.Source.BAZAAR, buy, sell, margin))
        }
        return results
    }

    /**
     * Best-effort AH scan: looks at the first [pages] pages of active BIN auctions and, per item
     * name, compares the cheapest listing to the second-cheapest — a rough proxy for "how much
     * cheaper is the lowest BIN than the rest of the market". This is a heuristic hint, not a
     * profit guarantee (auction house fees, item modifiers/enchants etc. are not accounted for).
     */
    fun fetchAuctionFlips(pages: Int = 3): List<FlipResult> {
        val cheapest = HashMap<String, Double>()
        val secondCheapest = HashMap<String, Double>()

        for (page in 0 until pages) {
            val body = get("$AUCTIONS_URL?page=$page") ?: break
            val root = runCatching { JsonParser.parseString(body).asJsonObject }.getOrNull() ?: break
            val auctions = root.getAsJsonArray("auctions") ?: break

            for (el in auctions) {
                val auction = runCatching { el.asJsonObject }.getOrNull() ?: continue
                if (auction.get("bin")?.asBoolean != true) continue
                if (auction.get("claimed")?.asBoolean == true) continue
                val name = auction.get("item_name")?.asString ?: continue
                val price = auction.get("starting_bid")?.asDouble ?: continue
                if (price <= 0) continue

                val current = cheapest[name]
                if (current == null || price < current) {
                    secondCheapest[name] = current ?: price
                    cheapest[name] = price
                } else {
                    val second = secondCheapest[name]
                    if (second == null || price < second) secondCheapest[name] = price
                }
            }

            val totalPages = root.get("totalPages")?.asInt ?: pages
            if (page + 1 >= totalPages) break
        }

        return cheapest.mapNotNull { (name, low) ->
            val second = secondCheapest[name] ?: return@mapNotNull null
            val margin = second - low
            if (margin <= 0) return@mapNotNull null
            FlipResult(name, FlipResult.Source.AUCTION_HOUSE, second, low, margin)
        }
    }

    private fun get(url: String): String? = try {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Accept", "application/json")
            .header("User-Agent", "Aetherion-Mod")
            .timeout(Duration.ofSeconds(15))
            .GET().build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
        if (response.statusCode() !in 200..299) {
            logger.warn("Hypixel API request to $url failed with status ${response.statusCode()}")
            null
        } else {
            response.body()
        }
    } catch (e: Exception) {
        logger.warn("Hypixel API request to $url failed", e)
        null
    }
}
