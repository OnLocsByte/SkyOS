package dev.freelocs.aetherion.feature

import org.slf4j.LoggerFactory

/** Central registry so every feature module gets initialized exactly once, in one place. */
object FeatureManager {
    private val logger = LoggerFactory.getLogger("Aetherion/Features")
    private val features = mutableListOf<Feature>()

    fun register(feature: Feature) {
        features.add(feature)
    }

    fun initAll() {
        features.forEach { feature ->
            runCatching { feature.init() }
                .onFailure { logger.error("Failed to initialize feature '${feature.id}'", it) }
        }
        logger.info("Initialized ${features.size} feature(s): ${features.joinToString { it.id }}")
    }

    fun all(): List<Feature> = features
}
