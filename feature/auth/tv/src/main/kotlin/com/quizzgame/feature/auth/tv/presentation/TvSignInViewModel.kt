package com.quizzgame.feature.auth.tv.presentation

import androidx.lifecycle.viewModelScope
import com.quizzgame.core.presentation.BaseViewModel
import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.domain.entity.AuthResult
import com.quizzgame.shared.auth.domain.entity.AuthSurface
import com.quizzgame.shared.auth.domain.scenario.OtpSignInScenario
import com.quizzgame.shared.auth.presentation.OtpCodeSession
import com.quizzgame.shared.auth.presentation.OtpRequestPolicy
import com.quizzgame.shared.auth.presentation.OtpSignInEnvironment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class TvSignInViewModel @Inject constructor(
    private val scenario: OtpSignInScenario,
    private val environment: OtpSignInEnvironment,
) : BaseViewModel<TvSignInState, TvSignInIntent, Nothing>() {

    private var methods = TvSignInStep.Methods(emptyList(), false)
    private val otp = OtpCodeSession(scenario, environment)
    private var operation: Job? = null

    init {
        loadMethods()
    }

    override fun createInitialState() = TvSignInState()

    override fun handleIntent(intent: TvSignInIntent) {
        val current = state.value
        if (current.failure == AuthFailure.StorageRecovery || current.step == TvSignInStep.Finished) return
        if (operation?.isActive == true && intent != TvSignInIntent.Back) return
        when (intent) {
            TvSignInIntent.Load         -> loadMethods()

            is TvSignInIntent.ChooseOtp -> chooseOtp(current, intent.method)
            is TvSignInIntent.Edit      -> edit(current, intent.value)

            TvSignInIntent.RequestCode  -> requestCode(current)
            TvSignInIntent.Verify       -> verify(current)
            TvSignInIntent.Back         -> back(current)
        }
    }

    private fun loadMethods() = launchOperation {
        val loading = state.value.copy(step = TvSignInStep.Loading, busy = false, failure = null)
        updateState { loading }
        when (val result = scenario.methods(AuthSurface.TV)) {
            is AuthResult.Success -> {
                ensureActive()
                methods = TvSignInStep.Methods(
                    result.value.filter(AuthMethod::usesOtp),
                    google = AuthMethod.GOOGLE in result.value,
                )
                val available = methods
                updateState { loading.copy(step = available) }
            }

            is AuthResult.Failed  -> {
                ensureActive()
                updateState { loading.copy(failure = result.failure) }
            }
        }
    }

    private fun chooseOtp(state: TvSignInState, method: AuthMethod) {
        val step = state.step as? TvSignInStep.Methods ?: return
        if (method !in step.otp) return
        updateState { state.copy(step = TvSignInStep.Identifier(method), failure = null) }
    }

    private fun edit(state: TvSignInState, value: String) {
        val step = when (val current = state.step) {
            is TvSignInStep.Identifier -> current.copy(input = OtpRequestPolicy.identifier(value))
            is TvSignInStep.Code       -> current.copy(form = current.form.withCode(value)).also {
                if (it.form != current.form) otp.codeEdited()
            }
            else                  -> return
        }
        updateState { state.copy(step = step, failure = OtpRequestPolicy.afterEdit(state.failure)) }
    }

    private fun back(state: TvSignInState) {
        val previous = when (val current = state.step) {
            is TvSignInStep.Code       -> TvSignInStep.Identifier(current.method, current.input)
            is TvSignInStep.Identifier -> methods
            else                  -> return
        }
        operation?.cancel()
        otp.reset()
        updateState { state.copy(step = previous, busy = false, failure = null) }
    }

    private fun requestCode(state: TvSignInState) {
        val elapsed = environment.elapsed()
        if (!state.canRequest(elapsed)) return
        when (val step = state.step) {
            is TvSignInStep.Identifier -> submit(state, operation = { otp.request(step.method, step.input) }) {
                TvSignInStep.Code(step.method, step.input, it)
            }

            is TvSignInStep.Code -> {
                if (step.form.resendWaitMillis(state.requestBlockedUntil, elapsed) > 0) return
                submit(state, operation = otp::resend) { step.copy(form = it) }
            }

            else -> Unit
        }
    }

    private fun verify(state: TvSignInState) {
        val step = state.step as? TvSignInStep.Code ?: return
        val elapsed = environment.elapsed()
        if (!step.form.complete || !state.canRequest(elapsed)) return
        if (step.form.isExpired(elapsed, state.failure)) {
            updateState { state.copy(failure = AuthFailure.Expired) }
            return
        }
        val cleared = step.copy(form = step.form.copy(code = ""))
        submit(state, invalidCodeStep = cleared, operation = { otp.verify(step.form.code) }) {
            TvSignInStep.Finished
        }
    }

    private fun <T> submit(
        state: TvSignInState,
        invalidCodeStep: TvSignInStep = state.step,
        operation: suspend () -> AuthResult<T>,
        onSuccess: (T) -> TvSignInStep,
    ) = launchOperation {
        updateState { state.copy(busy = true, failure = null) }
        val result = operation()
        ensureActive()
        when (result) {
            is AuthResult.Success -> updateState { TvSignInState(onSuccess(result.value)) }
            is AuthResult.Failed  -> {
                val failure = result.failure
                val step = if (failure == AuthFailure.InvalidCode) invalidCodeStep else state.step
                val blockedUntil = environment.blockedUntil(state.requestBlockedUntil, failure)
                updateState {
                    state.copy(
                        step = step,
                        busy = false,
                        failure = failure,
                        requestBlockedUntil = blockedUntil,
                    )
                }
            }
        }
    }

    private fun launchOperation(block: suspend CoroutineScope.() -> Unit) {
        operation = viewModelScope.launch(block = block)
    }
}
