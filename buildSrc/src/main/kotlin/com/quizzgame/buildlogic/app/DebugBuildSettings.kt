package com.quizzgame.buildlogic.app

import org.gradle.api.provider.Property

abstract class DebugBuildSettings {
    abstract val networkMode: Property<DebugNetworkMode>
    abstract val sessionScenario: Property<DebugSessionScenario>
    abstract val failNetworkOnce: Property<Boolean>
}

enum class DebugNetworkMode { REAL, MOCK }
enum class DebugSessionScenario { STORED, GUEST, AUTHENTICATED, UNAVAILABLE }
