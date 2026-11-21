package com.quizzgame.feature.auth.mobile.presentation

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
internal class MobileSignInViewModel @Inject constructor(
    private val scenario: OtpSignInScenario,
    private val environment: OtpSignInEnvironment,
) : BaseViewModel<MobileSignInState, MobileSignInIntent, Nothing>() {

    private var methods = MobileSignInStep.Methods(emptyList(), emptyList())
    private val otp = OtpCodeSession(scenario, environment)
    private var operation: Job? = null

    init {
        loadMethods()
    }

    override fun createInitialState() = MobileSignInState()

    override fun handleIntent(intent: MobileSignInIntent) {
        val current = state.value
        if (current.failure == AuthFailure.StorageRecovery || current.step == MobileSignInStep.Finished) return
        if (operation?.isActive == true && intent != MobileSignInIntent.Back) return
        when (intent) {
            MobileSignInIntent.Load         -> loadMethods()

            is MobileSignInIntent.ChooseOtp -> chooseOtp(current, intent.method)
            is MobileSignInIntent.Edit      -> edit(current, intent.value)

            MobileSignInIntent.RequestCode  -> requestCode(current)
            MobileSignInIntent.Verify       -> verify(current)
            MobileSignInIntent.Back         -> back(current)
        }
    }

    private fun loadMethods() = launchOperation {
        val loading = state.value.copy(step = MobileSignInStep.Loading, busy = false, failure = null)
        updateState { loading }
        when (val result = scenario.methods(AuthSurface.MOBILE)) {
            is AuthResult.Success -> {
                ensureActive()
                val (direct, providers) = result.value.partition(AuthMethod::usesOtp)
                methods = MobileSignInStep.Methods(direct, providers)
                val available = methods
                updateState { loading.copy(step = available) }
            }

            is AuthResult.Failed  -> {
                ensureActive()
                updateState { loading.copy(failure = result.failure) }
            }
        }
    }

    private fun chooseOtp(state: MobileSignInState, method: AuthMethod) {
        val step = state.step as? MobileSignInStep.Methods ?: return
        if (method !in step.otp) return
        updateState { state.copy(step = MobileSignInStep.Identifier(method), failure = null) }
    }

    private fun edit(state: MobileSignInState, value: String) {
        val step = when (val current = state.step) {
            is MobileSignInStep.Identifier -> current.copy(input = OtpRequestPolicy.identifier(value))
            is MobileSignInStep.Code       -> current.copy(form = current.form.withCode(value)).also {
                if (it.form != current.form) otp.codeEdited()
            }
            else                  -> return
        }
        updateState { state.copy(step = step, failure = OtpRequestPolicy.afterEdit(state.failure)) }
    }

    private fun back(state: MobileSignInState) {
        val previous = when (val current = state.step) {
            is MobileSignInStep.Code       -> MobileSignInStep.Identifier(current.method, current.input)
            is MobileSignInStep.Identifier -> methods
            else                  -> return
        }
        operation?.cancel()
        otp.reset()
        updateState { state.copy(step = previous, busy = false, failure = null) }
    }

    private fun requestCode(state: MobileSignInState) {
        val elapsed = environment.elapsed()
        if (!state.canRequest(elapsed)) return
        when (val step = state.step) {
            is MobileSignInStep.Identifier -> submit(state, operation = { otp.request(step.method, step.input) }) {
                MobileSignInStep.Code(step.method, step.input, it)
            }

            is MobileSignInStep.Code -> {
                if (step.form.resendWaitMillis(state.requestBlockedUntil, elapsed) > 0) return
                submit(state, operation = otp::resend) { step.copy(form = it) }
            }

            else -> Unit
        }
    }

    private fun verify(state: MobileSignInState) {
        val step = state.step as? MobileSignInStep.Code ?: return
        val elapsed = environment.elapsed()
        if (!step.form.complete || !state.canRequest(elapsed)) return
        if (step.form.isExpired(elapsed, state.failure)) {
            updateState { state.copy(failure = AuthFailure.Expired) }
            return
        }
        val cleared = step.copy(form = step.form.copy(code = ""))
        submit(state, invalidCodeStep = cleared, operation = { otp.verify(step.form.code) }) {
            MobileSignInStep.Finished
        }
    }

    private fun <T> submit(
        state: MobileSignInState,
        invalidCodeStep: MobileSignInStep = state.step,
        operation: suspend () -> AuthResult<T>,
        onSuccess: (T) -> MobileSignInStep,
    ) = launchOperation {
        updateState { state.copy(busy = true, failure = null) }
        val result = operation()
        ensureActive()
        when (result) {
            is AuthResult.Success -> updateState { MobileSignInState(onSuccess(result.value)) }
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
