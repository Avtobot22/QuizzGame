package com.quizzgame.core.session.lifecycle

import com.quizzgame.core.session.model.TokenPair
import com.quizzgame.core.session.storage.SessionRecord
import com.quizzgame.core.session.storage.SessionStorageException
import com.quizzgame.core.session.storage.SessionStorageFailure
import com.quizzgame.core.session.storage.SessionStore

internal class SessionFaultStore(
    initial: SessionRecord = SessionRecord(TEST_DEVICE_ID, fixturePair("A")),
) : SessionStore {
    @Volatile
    var durable = initial
    val failBefore = mutableMapOf<Int, SessionStorageFailure>()
    val failAfter = mutableMapOf<Int, SessionStorageFailure>()
    var readFailure: SessionStorageFailure? = null
    var beforeWrite: (SessionRecord) -> Unit = {}
    var reads = 0
    var writes = 0
    var closes = 0

    override fun read(): SessionRecord {
        reads++
        readFailure?.let { throw SessionStorageException(it) }
        return durable
    }

    override fun write(tokens: TokenPair?): SessionRecord = writeRecord(durable.updated(tokens = tokens))

    override fun writeRecord(record: SessionRecord): SessionRecord {
        writes++
        beforeWrite(record)
        failBefore.remove(writes)?.let { throw SessionStorageException(it) }
        durable = record
        failAfter.remove(writes)?.let { throw SessionStorageException(it) }
        return record
    }

    override fun close() {
        closes++
    }
}

internal fun fixturePair(label: String) = TokenPair("fixture-$label-access", "fixture-$label-refresh")
