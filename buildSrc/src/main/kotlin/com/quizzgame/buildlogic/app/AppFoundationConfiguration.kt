package com.quizzgame.buildlogic.app

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create

fun Project.configureAppFoundation(platform: String) {
    val network = extensions.create<NetworkBuildSettings>("quizzNetwork").apply { applyDefaults() }
    val debug = extensions.create<DebugBuildSettings>("quizzDebug").apply {
        networkMode.convention(DebugNetworkMode.MOCK)
        sessionScenario.convention(DebugSessionScenario.STORED)
        failNetworkOnce.convention(false)
    }
    val baseUrl = deploymentBaseUrl(platform)
    val mode = providers.gradleProperty("quizz.$platform.debugNetworkMode")
        .map { DebugNetworkMode.valueOf(it) }.orElse(debug.networkMode)
    val scenario = providers.gradleProperty("quizz.$platform.debugSessionScenario")
        .map { DebugSessionScenario.valueOf(it) }.orElse(debug.sessionScenario)
    val failOnce = providers.gradleProperty("quizz.$platform.debugFailNetworkOnce")
        .map { it.toBooleanStrict() }.orElse(debug.failNetworkOnce)

    extensions.configure<ApplicationExtension> {
        buildFeatures.buildConfig = true
    }
    extensions.configure<ApplicationAndroidComponentsExtension> {
        finalizeDsl { android ->
            android.configureNetwork(network, baseUrl.get())
            android.buildTypes.getByName("debug") {
                buildConfigField("String", "NETWORK_MODE", mode.get().name.javaString())
                buildConfigField("String", "DEBUG_SESSION_SCENARIO", scenario.get().name.javaString())
                buildConfigField("boolean", "DEBUG_FAIL_NETWORK_ONCE", failOnce.get().toString())
            }
            android.buildTypes.getByName("release") { buildConfigField("String", "NETWORK_MODE", "\"REAL\"") }
        }
    }
}

private fun ApplicationExtension.configureNetwork(network: NetworkBuildSettings, baseUrl: String) {
    defaultConfig {
        buildConfigField("String", "API_BASE_URL", baseUrl.javaString())
        buildConfigField("String", "CLIENT_PLATFORM", network.clientPlatform.get().headerValue("clientPlatform"))
        buildConfigField("String", "USER_AGENT", network.userAgent.get().headerValue("userAgent"))
        buildConfigField("int", "CONNECT_TIMEOUT_MS", network.connectTimeoutMs.get().positive("connectTimeoutMs"))
        buildConfigField("int", "READ_TIMEOUT_MS", network.readTimeoutMs.get().positive("readTimeoutMs"))
        buildConfigField("int", "WRITE_TIMEOUT_MS", network.writeTimeoutMs.get().positive("writeTimeoutMs"))
        buildConfigField("int", "CALL_TIMEOUT_MS", network.callTimeoutMs.get().positive("callTimeoutMs"))
    }
}

private fun String.headerValue(name: String): String {
    require(isNotBlank() && all { it in ' '..'~' }) { "quizzNetwork.$name must contain printable ASCII characters" }
    return javaString()
}

private fun Int.positive(name: String): String {
    require(this > 0) { "quizzNetwork.$name must be positive" }
    return toString()
}

private fun String.javaString(): String = "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""
