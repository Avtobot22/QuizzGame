package com.quizzgame.app.startup

import com.quizzgame.app.graph.AppGraph
import com.quizzgame.core.session.SessionState
import kotlinx.coroutines.flow.StateFlow

interface AppStartup {
    val state: StateFlow<AppStartupState>
    val sessionState: StateFlow<SessionState>
    suspend fun ensureStarted()
    suspend fun retryInitialization()
    suspend fun awaitGraph(): AppGraph
}
