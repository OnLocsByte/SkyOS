package dev.freelocs.aetherion.features

/** Central list of all features, in display order. */
object FeatureRegistry {
    private val backing = mutableListOf<AetherionFeature>()
    val features: List<AetherionFeature> get() = backing

    fun register(feature: AetherionFeature) {
        backing += feature
    }

    fun initAll() {
        backing.forEach { it.init() }
    }

    fun byCategory(): Map<String, List<AetherionFeature>> =
        backing.groupBy { it.category }
}
