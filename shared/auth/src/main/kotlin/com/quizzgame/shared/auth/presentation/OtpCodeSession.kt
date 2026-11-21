package com.quizzgame.shared.auth.presentation

import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.domain.entity.AuthResult
import com.quizzgame.shared.auth.domain.entity.otp.OtpSignInAttempt
import com.quizzgame.shared.auth.domain.scenario.OtpSignInScenario
import com.quizzgame.shared.auth.domain.scenario.map
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** Ведёт попытку OTP, которую экран начинает запросом кода и закрывает при уходе с формы. */
class OtpCodeSession(
    private val scenario: OtpSignInScenario,
    private val environment: OtpSignInEnvironment,
) {
    private var attempt: OtpSignInAttempt? = null

    suspend fun request(method: AuthMethod, identifier: String): AuthResult<OtpCodeInput> =
        scenario.request(method, identifier).map { showNewCode(it) }

    suspend fun resend(): AuthResult<OtpCodeInput> {
        val current = attempt ?: return AuthResult.Failed(AuthFailure.Expired)
        return scenario.resend(current).map { showNewCode(it) }
    }

    suspend fun verify(code: String): AuthResult<Unit> {
        val current = attempt ?: return AuthResult.Failed(AuthFailure.Expired)
        return scenario.verify(current, code).map {
            currentCoroutineContext().ensureActive()
            reset()
        }
    }

    fun codeEdited() {
        attempt?.forgetVerification()
    }

    fun reset() {
        attempt = null
    }

    private suspend fun showNewCode(next: OtpSignInAttempt): OtpCodeInput {
        currentCoroutineContext().ensureActive()
        attempt = next
        val challenge = next.challenge
        val expireSeconds = challenge.expiresAt.epochSecond - environment.now().epochSecond
        return OtpCodeInput(
            challenge,
            environment.deadlineAfter(expireSeconds),
            environment.deadlineAfter(challenge.resendAfterSeconds),
        )
    }
}
