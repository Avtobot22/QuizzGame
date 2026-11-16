package com.quizzgame.core.flags

import io.github.avtobot22.featureflags.Feature

interface AppFeatureFlags {
    fun guestAllowed(): Boolean
    fun catalogPrefetch(): CatalogPrefetch = CatalogPrefetch()
    fun catalogParallax(): Boolean = false
}

interface FeatureFlagsController {
    suspend fun refresh(): FeatureFlagsLoadResult
}

object AppFeatureDefinitions {
    val tvPrefetch = Feature.config("dashboard.prefetch", defaultParams = CatalogPrefetch()) { parameters, defaults ->
        CatalogPrefetch(
            depth = parameters.int("depth", defaults.depth, min = 1, max = 8),
            maxConcurrent = parameters.int("maxConcurrent", defaults.maxConcurrent, min = 1, max = 4),
        )
    }
    val tvParallax = Feature.boolean("dashboard.parallax", default = false)
    val tvGuest = Feature.boolean("dashboard.guestMode", default = false)
    val mobileGuest = Feature.boolean("joinRoom.guestMode", default = false)
}

data class CatalogPrefetch(val enabled: Boolean = false, val depth: Int = 2, val maxConcurrent: Int = 2)
