package com.quizzgame.feature.splash.mobile.presentation

import androidx.lifecycle.viewModelScope
import com.quizzgame.core.error.operation.ErrorOperationContext
import com.quizzgame.core.error.operation.OperationKind
import com.quizzgame.core.error.operation.RetrySafety
import com.quizzgame.core.error.policy.DefaultErrorDecisionPolicy
import com.quizzgame.core.presentation.BaseViewModel
import com.quizzgame.feature.splash.mobile.navigation.MobileSplashDestination
import com.quizzgame.shared.splash.domain.StartSplashUseCase
import com.quizzgame.shared.splash.domain.entity.SplashOutcome
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class MobileSplashViewModel @Inject constructor(private val startSplash: StartSplashUseCase) :
    BaseViewModel<MobileSplashUiState, MobileSplashIntent, Nothing>() {

    private var job: Job? = null

    init {
        start()
    }

    override fun createInitialState(): MobileSplashUiState = MobileSplashUiState.Loading

    override fun handleIntent(intent: MobileSplashIntent) {
        when (intent) {
            MobileSplashIntent.Retry             -> if (state.value is MobileSplashUiState.Failed) start()
            MobileSplashIntent.NavigationHandled -> updateState { current ->
                if (current is MobileSplashUiState.NavigationPending) {
                    MobileSplashUiState.NavigationComplete
                } else {
                    current
                }
            }

            MobileSplashIntent.SignIn            -> if (state.value is MobileSplashUiState.Failed) {
                updateState { MobileSplashUiState.NavigationPending(MobileSplashDestination.SIGN_IN) }
            }
        }
    }

    private fun start() {
        if (job?.isActive == true) return
        updateState { MobileSplashUiState.Loading }
        job = viewModelScope.launch {
            val next = when (val outcome = startSplash()) {
                SplashOutcome.Continue  -> MobileSplashUiState.NavigationPending(MobileSplashDestination.JOIN_ROOM)
                SplashOutcome.SignIn    -> MobileSplashUiState.NavigationPending(MobileSplashDestination.SIGN_IN)
                is SplashOutcome.Failed -> MobileSplashUiState.Failed(
                    DefaultErrorDecisionPolicy().decide(
                        outcome.failure,
                        ErrorOperationContext(OperationKind.INITIAL_LOAD, false, RetrySafety.SAFE_TO_REPEAT),
                    ),
                )
            }
            updateState { next }
        }
    }
}
