package com.quizzgame.app.network

import com.quizzgame.core.network.config.ApiBaseUrl
import com.quizzgame.core.network.config.NetworkConfiguration
import com.quizzgame.core.network.config.NetworkTimeouts
import com.quizzgame.core.network.config.PlatformHeaders
import com.quizzgame.core.session.refresh.api.RefreshEndpoint

data class AppNetworkSettings(
    val apiBaseUrl: String,
    val headers: PlatformHeaders,
    val refreshEndpoint: RefreshEndpoint,
    val timeouts: NetworkTimeouts,
    val networkMode: String,
) {
    fun realNetworkConfiguration(): NetworkConfiguration {
        if (apiBaseUrl.isBlank()) throw AppNetworkConfigurationException()
        return try {
            NetworkConfiguration(ApiBaseUrl.https(apiBaseUrl), headers, timeouts)
        } catch (invalid: IllegalArgumentException) {
            throw AppNetworkConfigurationException(invalid)
        }
    }
}

class AppNetworkConfigurationException(cause: Exception? = null) :
    Exception("Configure an HTTPS API URL before using REAL network mode", cause)
