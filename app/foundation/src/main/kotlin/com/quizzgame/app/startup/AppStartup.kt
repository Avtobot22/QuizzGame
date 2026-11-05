package com.quizzgame.app.startup

import com.quizzgame.core.session.lifecycle.SessionOperation
import com.quizzgame.core.session.lifecycle.SessionState
import kotlinx.coroutines.flow.StateFlow

interface AppStartup {
    val state: StateFlow<AppStartupState>
    val sessionState: StateFlow<SessionState>
    suspend fun ensureStarted()
    suspend fun retryInitialization()
    suspend fun accessGraph(): GraphAccessResult
    suspend fun logout(): SessionOperation
}
