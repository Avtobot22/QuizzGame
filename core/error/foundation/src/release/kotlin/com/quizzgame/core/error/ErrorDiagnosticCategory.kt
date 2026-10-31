package com.quizzgame.core.error

/** Стабильные семантические коды диагностики без backend-текстов и debug/mock-кодов. */
enum class ErrorDiagnosticCategory {
    OFFLINE_OR_UNREACHABLE,
    TIMEOUT,
    SERVICE_UNAVAILABLE,
    RATE_LIMITED,
    ACCESS_DENIED,
    CLIENT_REJECTED,
    OTHER_HTTP,
    AUTHENTICATION_REQUIRED,
    SESSION_REVOKED,
    INVALID_SERVER_DATA,
    STORAGE_READ,
    STORAGE_WRITE,
    STORAGE_CRYPTO,
    STORAGE_CORRUPT,
    STORAGE_UNKNOWN,
}
