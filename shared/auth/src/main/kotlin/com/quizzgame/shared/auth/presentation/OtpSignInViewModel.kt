package com.quizzgame.shared.auth.presentation

import androidx.lifecycle.viewModelScope
import com.quizzgame.core.presentation.BaseViewModel
import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.domain.entity.AuthResult
import com.quizzgame.shared.auth.domain.entity.otp.OtpSignInAttempt
import com.quizzgame.shared.auth.domain.scenario.OtpSignInScenario
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

abstract class OtpSignInViewModel protected constructor(
    private val scenario: OtpSignInScenario,
    private val environment: OtpSignInEnvironment,
) : BaseViewModel<OtpState, OtpIntent, Nothing>() {

    private var methods = emptyList<AuthMethod>()
    private var attempt: OtpSignInAttempt? = null
    private var operation: Job? = null

    init {
        loadMethods()
    }

    final override fun createInitialState() = OtpState()

    final override fun handleIntent(intent: OtpIntent) {
        val current = state.value
        if (current.failure == AuthFailure.StorageRecovery || current.step == OtpStep.Finished) return
        if (operation?.isActive == true && intent != OtpIntent.Back) return
        when (intent) {
            OtpIntent.Load      -> loadMethods()

            is OtpIntent.Choose -> updateState { current.chooseOtpMethod(intent.method) }
            is OtpIntent.Edit   -> {
                val next = current.withInput(intent.value)
                if (next.step != current.step) attempt?.forgetVerification()
                updateState { next }
            }

            OtpIntent.Send      -> launchOperation { send(current) }
            OtpIntent.Verify    -> launchOperation { verify(current) }
            OtpIntent.Resend    -> launchOperation { resend(current) }
            OtpIntent.Back      -> back(current)
        }
    }

    private fun loadMethods() = launchOperation {
        val loading = state.value.copy(step = OtpStep.Loading, busy = false, failure = null)
        updateState { loading }
        when (val result = scenario.methods(environment.surface)) {
            is AuthResult.Success -> {
                methods = result.value
                updateState { loading.copy(step = OtpStep.Methods(result.value)) }
            }

            is AuthResult.Failed  -> updateState { loading.copy(failure = result.failure) }
        }
    }

    private fun back(state: OtpState) {
        operation?.cancel()
        attempt = null
        val previous = when (val current = state.step) {
            is OtpStep.Code       -> OtpStep.Identifier(current.method, current.input)
            is OtpStep.Identifier -> OtpStep.Methods(methods)
            else                  -> return
        }
        updateState { state.copy(step = previous, busy = false, failure = null) }
    }

    private suspend fun send(state: OtpState) {
        val step = state.step as? OtpStep.Identifier ?: return
        if (!state.canRequest(environment.elapsed())) return
        updateState { state.copy(busy = true, failure = null) }
        when (val result = scenario.request(step.method, step.input)) {
            is AuthResult.Success -> showCode(step.method, step.input, result.value)
            is AuthResult.Failed  -> fail(state, result.failure)
        }
    }

    private suspend fun resend(state: OtpState) {
        val step = state.step as? OtpStep.Code ?: return
        val currentAttempt = attempt ?: return
        if (state.resendWaitMillis(step, environment.elapsed()) > 0) return
        updateState { state.copy(busy = true, failure = null) }
        when (val result = scenario.resend(currentAttempt)) {
            is AuthResult.Success -> showCode(step.method, step.input, result.value)
            is AuthResult.Failed  -> fail(state, result.failure)
        }
    }

    private suspend fun verify(state: OtpState) {
        val step = state.step as? OtpStep.Code ?: return
        val currentAttempt = attempt ?: return
        val time = environment.elapsed()
        if (step.code.length != step.challenge.codeLength || !state.canRequest(time)) return
        if (state.isCodeExpired(step, time)) {
            fail(state, AuthFailure.Expired)
            return
        }
        updateState { state.copy(busy = true, failure = null) }
        when (val result = scenario.verify(currentAttempt, step.code)) {
            is AuthResult.Success -> {
                attempt = null
                updateState { OtpState(OtpStep.Finished) }
            }

            is AuthResult.Failed  -> {
                val next = if (result.failure == AuthFailure.InvalidCode) {
                    state.copy(step = step.copy(code = ""))
                } else {
                    state
                }
                fail(next, result.failure)
            }
        }
    }

    private fun fail(state: OtpState, failure: AuthFailure) {
        val wait = (failure as? AuthFailure.RateLimited)?.retryAfterSeconds?.let(environment::deadlineAfter)
        updateState {
            state.copy(
                busy = false,
                failure = failure,
                requestBlockedUntil = maxOf(state.requestBlockedUntil, wait ?: 0),
            )
        }
    }

    private fun showCode(method: AuthMethod, input: String, next: OtpSignInAttempt) {
        attempt = next
        val challenge = next.challenge
        val expireSeconds = challenge.expiresAt.epochSecond - environment.now().epochSecond
        val step = OtpStep.Code(
            method,
            input,
            challenge,
            environment.deadlineAfter(expireSeconds),
            environment.deadlineAfter(challenge.resendAfterSeconds),
        )
        updateState { OtpState(step) }
    }

    private fun launchOperation(block: suspend () -> Unit) {
        operation = viewModelScope.launch { block() }
    }
}
