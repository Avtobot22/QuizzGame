package com.quizzgame.shared.auth.domain

import com.quizzgame.core.session.lifecycle.SessionChangeResult
import com.quizzgame.core.session.model.TokenPair

/** Фиксирует поколение сессии до выдачи серверных credentials. */
fun interface AuthSessionAccess {
    suspend fun forSignIn(): AuthSessionAttempt?
}

interface AuthSessionAttempt {
    val deviceId: String
    suspend fun signIn(tokens: TokenPair): SessionChangeResult
}
