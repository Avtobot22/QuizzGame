package com.quizzgame.debug.startup

import android.content.Context
import com.quizzgame.core.session.AndroidSession
import com.quizzgame.core.session.SessionManager
import com.quizzgame.core.session.SessionRecord
import com.quizzgame.core.session.SessionStorageException
import com.quizzgame.core.session.SessionStorageFailure
import com.quizzgame.core.session.SessionStore
import com.quizzgame.core.session.TokenPair
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

    override fun read(): SessionRecord {
        if (failRead) {
            failRead = false
            throw SessionStorageException(SessionStorageFailure.READ)
        }
        return SessionRecord(deviceId, tokens)
    }

    override fun write(tokens: TokenPair?): SessionRecord {
        this.tokens = tokens
        return read()
    }
}
