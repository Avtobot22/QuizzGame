package com.quizzgame.shared.auth.domain.usecase

import com.quizzgame.core.session.lifecycle.SessionChangeResult
import com.quizzgame.core.session.lifecycle.SessionSignInAttempt
import com.quizzgame.core.session.model.TokenPair
import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.AuthResult

internal suspend fun SessionSignInAttempt.commitAuthResult(result: AuthResult<TokenPair>): AuthResult<Unit> =
    when (result) {
        is AuthResult.Failed -> result
        is AuthResult.Success -> when (signIn(result.value)) {
            SessionChangeResult.Committed -> AuthResult.Success(Unit)
            is SessionChangeResult.NeedsStorageRecovery -> AuthResult.Failed(AuthFailure.StorageRecovery)
            SessionChangeResult.BusyOrBlocked, SessionChangeResult.Closed ->
                AuthResult.Failed(AuthFailure.SessionChanged)
        }
    }
