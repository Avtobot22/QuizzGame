package com.quizzgame.core.network

import okhttp3.Request

enum class AuthPolicy {
    NONE,
    OPTIONAL,
    REQUIRED,
}

enum class ReplayPolicy {
    NEVER,
    AFTER_REFRESH_ONCE,
}

data class RequestPolicy(
    val auth: AuthPolicy = AuthPolicy.NONE,
    val replay: ReplayPolicy = ReplayPolicy.NEVER,
)

internal fun RequestPolicy.forRequest(request: Request): RequestPolicy =
    if (auth != AuthPolicy.NONE && request.method == "GET" && request.body == null) {
        this
    } else {
        copy(replay = ReplayPolicy.NEVER)
    }
