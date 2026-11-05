package com.quizzgame.core.session.storage

import com.quizzgame.core.session.model.TokenPair
import java.io.Closeable
import java.io.IOException

interface SessionStore : Closeable {

    fun read(): SessionRecord

    fun write(tokens: TokenPair?): SessionRecord
    fun writeRecord(record: SessionRecord): SessionRecord
    override fun close() = Unit
}

enum class SessionStorageFailure {
    READ,
    WRITE,
    CRYPTO,
    CORRUPT,
}

class SessionStorageException(val failure: SessionStorageFailure) : IOException("Session storage failure: $failure")
