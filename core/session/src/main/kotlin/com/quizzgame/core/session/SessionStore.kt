package com.quizzgame.core.session

import java.io.Closeable
import java.io.IOException

interface SessionStore : Closeable {

    fun read(): SessionRecord

    fun write(tokens: TokenPair?): SessionRecord
    override fun close() = Unit
}

fun interface DeviceIdProvider {

    fun get(): String
}

enum class SessionStorageFailure {
    READ,
    WRITE,
    CRYPTO,
    CORRUPT,
}

class SessionStorageException(val failure: SessionStorageFailure) : IOException("Session storage failure: $failure")

class SessionUnavailableException : IOException("Session record is unavailable")
