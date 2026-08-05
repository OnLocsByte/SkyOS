package dev.freelocs.aetherion.features.bazaar

import com.google.gson.JsonParser
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration

/**
 * Thin wrapper around the public Hypixel API (bazaar + auction house).
 *
 * Every endpoint requires a personal API key generated at
 * https://developer.hypixel.net — the key lives only in the local config
 * file and is sent as the "API-Key" request header, never bundled with the
 * mod or sent anywhere else.
 */
object HypixelApiClient {

    private const val BAZAAR_URL = "https://api.hypixel.net/v2/skyblock/bazaar"
    private const val AUCTIONS_URL = "https://api.hypixel.net/v2/skyblock/auctions?page=0"

    private val HTTP: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()

    data class BazaarProduct(
        val productId: String,
        val buyPrice: Double,
        val sellPrice: Double,
        val buyVolume: Long,
        val sellVolume: Long
    )

    data class AuctionEntry(
        val itemName: String,
        val bin: Boolean,
        val startingBid: Long
    )

    fun fetchBazaar(apiKey: String): List<BazaarProduct> {
        val body = get(BAZAAR_URL, apiKey)
        val root = JsonParser.parseString(body).asJsonObject
        if (!root.get("success").asBoolean) throw IOException("Bazaar request unsuccessful")

        val products = root.getAsJsonObject("products")
        val result = mutableListOf<BazaarProduct>()
        for ((id, element) in products.entrySet()) {
            val quickStatus = element.asJsonObject.getAsJsonObject("quick_status") ?: continue
            result.add(
                BazaarProduct(
                    productId = id,
                    buyPrice = quickStatus.get("buyPrice")?.asDouble ?: 0.0,
                    sellPrice = quickStatus.get("sellPrice")?.asDouble ?: 0.0,
                    buyVolume = quickStatus.get("buyVolume")?.asLong ?: 0L,
                    sellVolume = quickStatus.get("sellVolume")?.asLong ?: 0L
                )
            )
        }
        return result
    }

    fun fetchAuctionsPage0(apiKey: String): List<AuctionEntry> {
        val body = get(AUCTIONS_URL, apiKey)
        val root = JsonParser.parseString(body).asJsonObject
        if (!root.get("success").asBoolean) throw IOException("Auctions request unsuccessful")

        val auctions = root.getAsJsonArray("auctions") ?: return emptyList()
        val result = mutableListOf<AuctionEntry>()
        for (el in auctions) {
            val obj = el.asJsonObject
            val bin = obj.get("bin")?.asBoolean ?: false
            if (!bin) continue
            val name = obj.get("item_name")?.asString ?: continue
            val bid = obj.get("starting_bid")?.asLong ?: continue
            result.add(AuctionEntry(name, bin, bid))
        }
        return result
    }

    private fun get(url: String, apiKey: String): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("API-Key", apiKey)
            .header("Accept", "application/json")
            .timeout(Duration.ofSeconds(15))
            .GET().build()
        val response = HTTP.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
        if (response.statusCode() !in 200..299) {
            throw IOException("Hypixel API returned ${response.statusCode()} for $url")
        }
        return response.body()
    }
}
