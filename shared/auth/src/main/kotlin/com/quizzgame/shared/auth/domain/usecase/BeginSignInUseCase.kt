package com.quizzgame.shared.auth.domain.usecase

import com.quizzgame.core.session.lifecycle.SessionActions
import com.quizzgame.core.session.lifecycle.SessionSignInAttempt
import javax.inject.Inject

internal class BeginSignInUseCase @Inject constructor(private val sessionActions: SessionActions) {
    suspend operator fun invoke(): SessionSignInAttempt? = sessionActions.beginSignIn()
}
