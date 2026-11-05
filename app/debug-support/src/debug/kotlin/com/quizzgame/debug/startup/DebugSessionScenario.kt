package com.quizzgame.debug.startup

import android.content.Context
import com.quizzgame.core.session.lifecycle.SessionManager
import com.quizzgame.core.session.model.TokenPair
import com.quizzgame.core.session.platform.AndroidSession
import com.quizzgame.core.session.storage.SessionRecord
import com.quizzgame.core.session.storage.SessionStorageException
import com.quizzgame.core.session.storage.SessionStorageFailure
import com.quizzgame.core.session.storage.SessionStore
import java.util.UUID

suspend fun openDebugSession(context: Context, scenario: String): SessionManager = when (scenario) {
    "STORED" -> AndroidSession.open(context)
    "GUEST", "AUTHENTICATED", "UNAVAILABLE" -> SessionManager.open(ScenarioSessionStore(scenario))
    else -> error("Unknown debug session scenario")
}

private class ScenarioSessionStore(scenario: String) : SessionStore {
    private val deviceId = UUID.randomUUID().toString()
    private var failRead = scenario == "UNAVAILABLE"
    private var tokens = if (scenario == "AUTHENTICATED") {
        TokenPair("fixture-demo-access", "fixture-demo-refresh")
    } else {
        null
    }

    private var record = SessionRecord(deviceId, tokens)

    override fun read(): SessionRecord {
        if (failRead) {
            failRead = false
            throw SessionStorageException(SessionStorageFailure.READ)
        }
        return record
    }

    override fun writeRecord(record: SessionRecord): SessionRecord {
        this.record = record
        return record
    }

    override fun write(tokens: TokenPair?): SessionRecord {
        return writeRecord(SessionRecord(deviceId, tokens))
    }
}
