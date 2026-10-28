package com.quizzgame.debug.network

import com.quizzgame.core.network.NetworkTimeouts
import com.quizzgame.core.network.PlatformHeaders

enum class DebugNetworkMode {
    REAL,
    MOCK,
}

data class DebugNetworkConfiguration(
    val mode: DebugNetworkMode,
    val headers: PlatformHeaders,
    val realBaseUrl: String? = null,
    val timeouts: NetworkTimeouts = NetworkTimeouts(),
) {

    init {
        require(mode != DebugNetworkMode.REAL || !realBaseUrl.isNullOrBlank()) {
            "REAL mode requires an externally configured HTTPS API URL"
        }
        require(mode != DebugNetworkMode.MOCK || realBaseUrl == null) {
            "MOCK mode must not configure a remote fallback URL"
        }
    }
}
