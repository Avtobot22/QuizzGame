package com.quizzgame.core.network

import okhttp3.Headers
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class PlatformHeaders(
    val clientPlatform: String,
    val userAgent: String,
    val apiVersion: Int,
) {

    init {
        require(apiVersion > 0) { "API version must be positive" }
        require(clientPlatform.isNotBlank() && userAgent.isNotBlank()) { "Platform headers must not be blank" }
        Headers.Builder()
            .add(HttpHeaderNames.X_CLIENT_PLATFORM, clientPlatform)
            .add(HttpHeaderNames.USER_AGENT, userAgent)
    }
}

data class NetworkTimeouts(
    val connect: Duration = 10.seconds,
    val read: Duration = 30.seconds,
    val write: Duration = 30.seconds,
    val call: Duration = 30.seconds,
) {

    init {
        require(listOf(connect, read, write, call).all { it.isFinite() && it.inWholeMilliseconds > 0 }) {
            "Network timeouts must be finite and at least one millisecond"
        }
    }
}

data class NetworkConfiguration(
    val baseUrl: ApiBaseUrl,
    val headers: PlatformHeaders,
    val timeouts: NetworkTimeouts = NetworkTimeouts(),
)
