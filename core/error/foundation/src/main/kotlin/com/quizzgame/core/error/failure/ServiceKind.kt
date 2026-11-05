package com.quizzgame.core.error.failure

enum class ServiceKind {
    UNAVAILABLE,
    RATE_LIMITED,
    ACCESS_DENIED,
    CLIENT_REJECTED,
    OTHER_HTTP,
}
