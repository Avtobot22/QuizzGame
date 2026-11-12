package com.quizzgame.core.flags

import io.github.avtobot22.featureflags.Feature

interface AppFeatureFlags {
    fun guestAllowed(): Boolean
}

interface FeatureFlagsController {
    suspend fun refresh(): FeatureFlagsLoadResult
}

object AppFeatureDefinitions {
    val tvGuest = Feature.boolean("dashboard.guestMode", default = false)
    val mobileGuest = Feature.boolean("joinRoom.guestMode", default = false)
}
