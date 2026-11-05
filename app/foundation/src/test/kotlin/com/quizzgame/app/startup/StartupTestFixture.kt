package com.quizzgame.app.startup

import com.quizzgame.app.network.AppNetworkGraph
import com.quizzgame.app.network.AppNetworkGraphFactory
import com.quizzgame.core.network.api.ApiRuntime
import com.quizzgame.core.network.result.NetworkResultAdapter
import com.quizzgame.core.session.model.TokenPair
import com.quizzgame.core.session.storage.SessionRecord
import com.quizzgame.core.session.storage.SessionStorageException
import com.quizzgame.core.session.storage.SessionStorageFailure
import com.quizzgame.core.session.storage.SessionStore
import retrofit2.Retrofit
import java.io.IOException

internal fun fakeNetworkFactory(onClose: () -> Unit = {}): AppNetworkGraphFactory =
    AppNetworkGraphFactory { _, json ->
        AppNetworkGraph(
            ApiRuntime(Retrofit.Builder().baseUrl("https://example.test/").build(), NetworkResultAdapter(json)),
            onClose,
        )
    }

internal class StartupTestStore(
    private val events: MutableList<String> = mutableListOf(),
    initialTokens: TokenPair? = null,
) : SessionStore {
    var failRead = false
    var readFailure: SessionStorageFailure? = null
    var writeFailure: SessionStorageFailure? = null
    var reads = 0
    var writes = 0
    var closes = 0
    private var stored = SessionRecord("123e4567-e89b-12d3-a456-426614174000", initialTokens)
    val tokens get() = stored.tokens

    override fun read(): SessionRecord {
        reads++
        if (failRead) throw IOException("Unreadable local store")
        readFailure?.let { throw SessionStorageException(it) }
        return stored
    }

    override fun write(tokens: TokenPair?): SessionRecord {
        writes++
        writeFailure?.let { throw SessionStorageException(it) }
        stored = SessionRecord(stored.deviceId, tokens)
        return stored
    }

    override fun writeRecord(record: SessionRecord): SessionRecord {
        writes++
        writeFailure?.let { throw SessionStorageException(it) }
        stored = record
        return stored
    }

    override fun close() {
        closes++
        events += "session-close"
    }
}
