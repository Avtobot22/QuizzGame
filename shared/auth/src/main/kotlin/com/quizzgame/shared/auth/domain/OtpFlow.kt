package com.quizzgame.shared.auth.domain

import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.domain.entity.AuthResult
import com.quizzgame.shared.auth.domain.entity.AuthSurface
import com.quizzgame.shared.auth.domain.entity.OtpChallenge
import com.quizzgame.shared.auth.domain.entity.OtpIntent
import com.quizzgame.shared.auth.domain.entity.OtpState
import com.quizzgame.shared.auth.domain.entity.OtpStep
import java.time.Instant
import java.util.UUID

/** Состояние попытки живёт только в ViewModel. Таймеры используют монотонное время. */
class OtpFlow(
    private val auth: AuthUseCase,
    private val surface: AuthSurface,
    private val elapsed: () -> Long,
    private val now: () -> Instant = Instant::now,
) {
    private var methods = emptyList<AuthMethod>()
    private var session: AuthSessionAttempt? = null
    private var verification: Verification? = null
    private var storageRecoveryRequired = false

    suspend fun handle(intent: OtpIntent, state: OtpState, publish: (OtpState) -> Unit) {
        when (intent) {
            OtpIntent.Load -> load(publish)
            is OtpIntent.Choose -> if (state.step is OtpStep.Methods && intent.method in methods &&
                intent.method in OTP_METHODS
            ) {
                publish(OtpState(OtpStep.Identifier(intent.method)))
            }
            is OtpIntent.Edit -> publish(edit(state, intent.value))
            OtpIntent.Send -> send(state, publish)
            OtpIntent.Verify -> if (!storageRecoveryRequired) verify(state, publish)
            OtpIntent.Resend -> if (!storageRecoveryRequired) resend(state, publish)
            OtpIntent.Back -> publish(back(state))
        }
    }

    private suspend fun load(publish: (OtpState) -> Unit) {
        publish(OtpState(busy = true))
        when (val result = auth.methods(surface)) {
            is AuthResult.Success -> {
                methods = if (surface == AuthSurface.TV) result.value.filter { it in OTP_METHODS } else result.value
                publish(OtpState(OtpStep.Methods(methods)))
            }
            is AuthResult.Failed -> publish(OtpState(failure = result.failure))
        }
    }

    private fun back(state: OtpState): OtpState {
        verification = null
        session = null
        return when (val current = state.step) {
            is OtpStep.Code -> OtpState(OtpStep.Identifier(current.method, current.input))
            is OtpStep.Identifier -> OtpState(OtpStep.Methods(methods))
            else -> state.copy(busy = false)
        }
    }

    private fun edit(state: OtpState, value: String): OtpState {
        val step = when (val current = state.step) {
            is OtpStep.Identifier -> current.copy(input = value.take(MAX_IDENTIFIER_LENGTH))
            is OtpStep.Code -> current.copy(code = value.filter { it in '0'..'9' }.take(current.challenge.codeLength))
            else -> return state
        }
        if (step != state.step) verification = null
        return state.copy(
            step = step,
            failure = state.failure.takeUnless {
                it == AuthFailure.InvalidCode || it == AuthFailure.InvalidIdentifier
            },
        )
    }

    private suspend fun send(state: OtpState, publish: (OtpState) -> Unit) {
        val step = state.step as? OtpStep.Identifier ?: return
        if (elapsed() < state.requestBlockedUntil) return
        publish(state.copy(busy = true, failure = null))
        val attempt = auth.begin()
        if (attempt == null) return fail(state, AuthFailure.SessionChanged, publish)
        when (val result = auth.request(step.method, step.input)) {
            is AuthResult.Success -> {
                session = attempt
                publish(OtpState(codeStep(step.method, step.input, result.value)))
            }
            is AuthResult.Failed -> fail(state, result.failure, publish)
        }
    }

    private suspend fun resend(state: OtpState, publish: (OtpState) -> Unit) {
        val step = state.step as? OtpStep.Code ?: return
        if (elapsed() < maxOf(step.resendAtMillis, state.requestBlockedUntil)) return
        publish(state.copy(busy = true, failure = null))
        when (val result = auth.resend(step.challenge)) {
            is AuthResult.Success -> {
                verification = null
                publish(OtpState(codeStep(step.method, step.input, result.value)))
            }
            is AuthResult.Failed -> fail(state, result.failure, publish)
        }
    }

    private suspend fun verify(state: OtpState, publish: (OtpState) -> Unit) {
        val step = state.step as? OtpStep.Code ?: return
        val attempt = session ?: return
        val time = elapsed()
        if (step.code.length != step.challenge.codeLength || time < state.requestBlockedUntil) return
        if (time >= step.expiresAtMillis || state.failure in setOf(AuthFailure.Expired, AuthFailure.Consumed)) {
            return fail(state, AuthFailure.Expired, publish)
        }
        val key = verification?.takeIf { it.code == step.code }?.key ?: UUID.randomUUID().toString()
        verification = Verification(step.code, key)
        publish(state.copy(busy = true, failure = null))
        when (val result = auth.verify(attempt, step.challenge.id, step.code, key)) {
            is AuthResult.Success -> {
                session = null
                verification = null
                publish(OtpState(OtpStep.Finished))
            }
            is AuthResult.Failed -> if (result.failure == AuthFailure.InvalidCode) {
                verification = null
                fail(state.copy(step = step.copy(code = "")), result.failure, publish)
            } else {
                fail(state, result.failure, publish)
            }
        }
    }

    private fun fail(state: OtpState, failure: AuthFailure, publish: (OtpState) -> Unit) {
        storageRecoveryRequired = failure == AuthFailure.StorageRecovery
        val wait = (failure as? AuthFailure.RateLimited)?.retryAfterSeconds
        publish(
            state.copy(
                busy = false,
                failure = failure,
                requestBlockedUntil = maxOf(state.requestBlockedUntil, wait?.let(::deadline) ?: 0),
            ),
        )
    }

    private fun codeStep(method: AuthMethod, input: String, challenge: OtpChallenge): OtpStep.Code {
        val expireSeconds = challenge.expiresAt.epochSecond - now().epochSecond
        return OtpStep.Code(method, input, challenge, deadline(expireSeconds), deadline(challenge.resendAfterSeconds))
    }

    private fun deadline(seconds: Long): Long = elapsed() + seconds.coerceIn(0, Long.MAX_VALUE / MILLIS - 1) * MILLIS

    private class Verification(val code: String, val key: String)

    private companion object {
        val OTP_METHODS = setOf(AuthMethod.EMAIL, AuthMethod.SMS)
        const val MILLIS = 1000
        const val MAX_IDENTIFIER_LENGTH = 254
    }
}
