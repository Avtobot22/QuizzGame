package com.quizzgame.buildlogic.app

import org.gradle.api.provider.Property

abstract class NetworkBuildSettings {
    abstract val clientPlatform: Property<String>
    abstract val userAgent: Property<String>
    abstract val apiVersion: Property<Int>
    abstract val connectTimeoutMs: Property<Int>
    abstract val readTimeoutMs: Property<Int>
    abstract val writeTimeoutMs: Property<Int>
    abstract val callTimeoutMs: Property<Int>
}

internal fun NetworkBuildSettings.applyDefaults() {
    apiVersion.convention(NetworkDefaults.API_VERSION)
    connectTimeoutMs.convention(NetworkDefaults.CONNECT_TIMEOUT_MS)
    readTimeoutMs.convention(NetworkDefaults.REQUEST_TIMEOUT_MS)
    writeTimeoutMs.convention(NetworkDefaults.REQUEST_TIMEOUT_MS)
    callTimeoutMs.convention(NetworkDefaults.REQUEST_TIMEOUT_MS)
}

private object NetworkDefaults {
    const val API_VERSION = 1
    const val CONNECT_TIMEOUT_MS = 10_000
    const val REQUEST_TIMEOUT_MS = 30_000
}
