package com.quizzgame.feature.authprovider.tv.presentation

import androidx.lifecycle.viewModelScope
import com.quizzgame.core.presentation.BaseViewModel
import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.AuthResult
import com.quizzgame.shared.auth.domain.scenario.AuthorizeTvDeviceScenario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class TvDeviceAuthViewModel @Inject constructor(
    private val scenario: AuthorizeTvDeviceScenario,
) : BaseViewModel<TvDeviceAuthState, TvDeviceAuthIntent, Nothing>() {
    private val foreground = MutableStateFlow(false)
    private var operation: Job? = null

    val available get() = scenario.isAvailable

    override fun createInitialState(): TvDeviceAuthState = TvDeviceAuthState.Idle

    override fun handleIntent(intent: TvDeviceAuthIntent) {
        when (intent) {
            TvDeviceAuthIntent.Start -> start()
            is TvDeviceAuthIntent.Foreground -> foreground.value = intent.active
            TvDeviceAuthIntent.Back -> if (state.value !is TvDeviceAuthState.Confirming) {
                operation?.cancel()
                updateState { TvDeviceAuthState.Idle }
            }
        }
    }

    private fun start() {
        val current = state.value
        if (!available) return
        if (operation?.isActive == true || current is TvDeviceAuthState.Confirming ||
            (current as? TvDeviceAuthState.Failed)?.failure == AuthFailure.StorageRecovery
        ) {
            return
        }
        updateState { TvDeviceAuthState.Starting }
        operation = viewModelScope.launch {
            val result = scenario(
                awaitForeground = { foreground.first { it } },
                onWaiting = { authorization -> updateState { TvDeviceAuthState.Waiting(authorization) } },
                onApproved = { authorization -> updateState { TvDeviceAuthState.Confirming(authorization) } },
            )
            if (result is AuthResult.Failed) updateState { TvDeviceAuthState.Failed(result.failure) }
        }
    }
}
