package com.quizzgame.core.error

/** Стабильные ключи локального текста для адаптеров Mobile/TV; backend-сообщения здесь не используются. */
enum class ErrorCopyKey {
    CONNECTION_PROBLEM,
    REQUEST_TIMED_OUT,
    SERVER_UNAVAILABLE,
    TOO_MANY_REQUESTS,
    ACCESS_DENIED,
    SIGN_IN_REQUIRED,
    SESSION_EXPIRED,
    UNEXPECTED_RESPONSE,
    STORAGE_PROBLEM,
    GENERIC_FAILURE,
}
