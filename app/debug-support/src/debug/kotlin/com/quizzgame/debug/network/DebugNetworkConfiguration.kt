package com.quizzgame.debug.network

import com.quizzgame.core.network.config.NetworkTimeouts
import com.quizzgame.core.network.config.PlatformHeaders

enum class DebugNetworkMode {
    REAL,
    MOCK,
}

data class DebugNetworkConfiguration(
    val mode: DebugNetworkMode,
    val headers: PlatformHeaders,
    val realBaseUrl: String? = null,
    val timeouts: NetworkTimeouts = NetworkTimeouts(),
    val externalLoopbackPort: Int = 0,
) {

    init {
        require(externalLoopbackPort in 0..MAX_PORT)
        require(externalLoopbackPort == 0 || mode == DebugNetworkMode.MOCK)
        require(mode != DebugNetworkMode.REAL || !realBaseUrl.isNullOrBlank()) {
            "REAL mode requires an externally configured HTTPS API URL"
        }
        require(mode != DebugNetworkMode.MOCK || realBaseUrl == null) {
            "MOCK mode must not configure a remote fallback URL"
        }
    }
}

private const val MAX_PORT = 65_535
