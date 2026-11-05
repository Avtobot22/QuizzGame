package com.quizzgame.core.network.config

import kotlinx.serialization.json.Json

fun createNetworkJson(): Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = true
    isLenient = false
    coerceInputValues = false
}
