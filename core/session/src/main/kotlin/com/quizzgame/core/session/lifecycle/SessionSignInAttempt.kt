package com.quizzgame.core.session.lifecycle

import com.quizzgame.core.session.model.TokenPair

class SessionSignInAttempt internal constructor(
    val deviceId: String,
    private val commit: suspend (TokenPair) -> SessionChangeResult,
) {

    suspend fun signIn(tokens: TokenPair): SessionChangeResult = commit(tokens)
}
