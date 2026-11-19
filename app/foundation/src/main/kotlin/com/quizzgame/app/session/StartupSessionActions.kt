package com.quizzgame.app.session

import com.quizzgame.app.startup.AppStartup
import com.quizzgame.app.startup.GraphAccessResult
import com.quizzgame.core.session.lifecycle.SessionActions
import com.quizzgame.core.session.lifecycle.SessionChangeResult
import com.quizzgame.core.session.lifecycle.SessionRecoveryResult
import com.quizzgame.core.session.model.TokenPair

internal class StartupSessionActions(private val startup: AppStartup) : SessionActions {
    override suspend fun beginSignIn() = current()?.beginSignIn()

    override suspend fun signIn(tokens: TokenPair) = current()?.signIn(tokens) ?: SessionChangeResult.BusyOrBlocked

    override suspend fun signOut() = current()?.signOut() ?: SessionChangeResult.BusyOrBlocked

    override suspend fun recoverStorage() = current()?.recoverStorage() ?: SessionRecoveryResult.NotAllowed

    private suspend fun current() = (startup.accessGraph() as? GraphAccessResult.Ready)?.graph?.session
}
