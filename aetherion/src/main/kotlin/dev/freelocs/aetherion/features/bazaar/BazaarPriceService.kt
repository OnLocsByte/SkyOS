package dev.freelocs.aetherion.features.bazaar

import com.google.gson.JsonParser
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

/** A single bazaar "flip" opportunity: the spread between instant-buy and instant-sell price. */
data class BazaarFlip(
    val productId: String,
    val buyPrice: Double,
    val sellPrice: Double,
    val netProfitPerUnit: Double,
    val marginFraction: Double,
)

/**
 * Read-only Bazaar price checker. Fetches Hypixel's public Bazaar endpoint
 * (no API key required) and highlights products with a large spread
 * between instant-buy and instant-sell price — i.e. what a limit-order
 * flip (buy near the low sell-offer price, resell near the high buy-order
 * price) could theoretically capture over time. This is display-only:
 * nothing here places orders or buys/sells anything automatically.
 */
object BazaarPriceService {
    private const val BAZAAR_ENDPOINT = "https://api.hypixel.net/v2/skyblock/bazaar"

    // Hypixel charges tax when placing bazaar orders (currently ~1.25% per order).
    // A flip needs one buy order and one sell order, so this approximates the
    // combined cost. It's an estimate — always sanity-check against the
    // in-game order confirmation before trading on it.
    private const val ESTIMATED_ORDER_TAX_RATE = 0.0125 * 2

    private val logger = LoggerFactory.getLogger("Aetherion/Bazaar")
    private val executor = Executors.newSingleThreadExecutor { r -> Thread(r, "Aetherion-Bazaar").apply { isDaemon = true } }
    private val http: HttpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()

    @Volatile var lastFlips: List<BazaarFlip> = emptyList()
        private set
    @Volatile var lastError: String? = null
        private set
    @Volatile var lastFetchedAtMillis: Long = 0L
        private set
    @Volatile private var refreshing = false

    fun refreshAsync(minProfitPerUnit: Double, minMargin: Double, maxResults: Int) {
        if (refreshing) return
        refreshing = true
        CompletableFuture.supplyAsync({ fetchFlips(minProfitPerUnit, minMargin, maxResults) }, executor)
            .whenComplete { result, error ->
                if (error != null) {
                    logger.warn("Bazaar flip refresh failed", error)
                    lastError = error.message ?: error.cause?.message ?: "Unknown error"
                } else {
                    lastFlips = result
                    lastError = null
                    lastFetchedAtMillis = System.currentTimeMillis()
                }
                refreshing = false
            }
    }

    private fun fetchFlips(minProfitPerUnit: Double, minMargin: Double, maxResults: Int): List<BazaarFlip> {
        val request = HttpRequest.newBuilder(URI.create(BAZAAR_ENDPOINT))
            .header("Accept", "application/json")
            .timeout(Duration.ofSeconds(15))
            .GET().build()
        val response = http.send(request, HttpResponse.BodyHandlers.ofString())
        check(response.statusCode() in 200..299) { "Hypixel API returned HTTP ${response.statusCode()}" }

        val root = JsonParser.parseString(response.body()).asJsonObject
        check(root.get("success")?.asBoolean == true) { "Hypixel API reported success=false" }
        val products = root.getAsJsonObject("products") ?: return emptyList()

        val flips = mutableListOf<BazaarFlip>()
        for ((productId, element) in products.entrySet()) {
            val status = element.asJsonObject.getAsJsonObject("quick_status") ?: continue
            val buyPrice = status.get("buyPrice")?.asDouble ?: continue
            val sellPrice = status.get("sellPrice")?.asDouble ?: continue
            if (buyPrice <= 0.0 || sellPrice <= 0.0) continue

            val grossProfit = buyPrice - sellPrice
            val netProfit = grossProfit - (buyPrice * ESTIMATED_ORDER_TAX_RATE)
            if (netProfit < minProfitPerUnit) continue

            val margin = netProfit / sellPrice
            if (margin < minMargin) continue

            flips += BazaarFlip(productId, buyPrice, sellPrice, netProfit, margin)
        }
        return flips.sortedByDescending { it.marginFraction }.take(maxResults)
    }
}
