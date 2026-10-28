package com.quizzgame.core.network

internal object HttpHeaderNames {

    const val ACCEPT = "Accept"
    const val AUTHORIZATION = "Authorization"
    const val RETRY_AFTER = "Retry-After"
    const val USER_AGENT = "User-Agent"
    const val X_CLIENT_PLATFORM = "X-Client-Platform"
    const val X_ENDPOINT_VERSION = "X-Endpoint-Version"
}

internal object HttpHeaderValues {

    const val APPLICATION_JSON = "application/json"
    const val RETRY_AFTER_IMMEDIATE_SECONDS = 0L
    const val RETRY_AFTER_DEFERRED_SECONDS = 1L
}
