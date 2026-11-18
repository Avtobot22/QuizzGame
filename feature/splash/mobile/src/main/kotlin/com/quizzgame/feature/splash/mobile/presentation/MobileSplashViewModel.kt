package com.quizzgame.feature.splash.mobile.presentation

import androidx.lifecycle.viewModelScope
import com.quizzgame.core.error.operation.ErrorOperationContext
import com.quizzgame.core.error.operation.OperationKind
import com.quizzgame.core.error.operation.RetrySafety
import com.quizzgame.core.error.policy.DefaultErrorDecisionPolicy
import com.quizzgame.core.presentation.BaseViewModel
import com.quizzgame.feature.splash.mobile.navigation.MobileSplashDestination
import com.quizzgame.shared.splash.domain.StartSplashUseCase
import com.quizzgame.shared.splash.domain.entity.SplashResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class MobileSplashViewModel @Inject constructor(private val startSplashUseCase: StartSplashUseCase) :
    BaseViewModel<MobileSplashUiState, MobileSplashIntent, Nothing>() {

    private var job: Job? = null

    init {
        start()
    }

    override fun createInitialState(): MobileSplashUiState = MobileSplashUiState.Loading

    override fun handleIntent(intent: MobileSplashIntent) {
        when (intent) {
            MobileSplashIntent.Retry             -> if (state.value is MobileSplashUiState.Error) start()
            MobileSplashIntent.NavigationHandled -> updateState { current ->
                if (current is MobileSplashUiState.NavigationPending) {
                    MobileSplashUiState.NavigationComplete
                } else {
                    current
                }
            }

            MobileSplashIntent.SignIn            -> if (state.value is MobileSplashUiState.Error) {
                updateState { MobileSplashUiState.NavigationPending(MobileSplashDestination.SIGN_IN) }
            }
        }
    }

    private fun start() {
        if (job?.isActive == true) return
        updateState { MobileSplashUiState.Loading }
        job = viewModelScope.launch {
            val next = when (val result = startSplashUseCase()) {
                SplashResult.Continue  -> MobileSplashUiState.NavigationPending(MobileSplashDestination.PLAY)
                SplashResult.SignIn    -> MobileSplashUiState.NavigationPending(MobileSplashDestination.SIGN_IN)
                is SplashResult.Failed -> MobileSplashUiState.Error(
                    DefaultErrorDecisionPolicy().decide(
                        result.failure,
                        ErrorOperationContext(OperationKind.INITIAL_LOAD, false, RetrySafety.SAFE_TO_REPEAT),
                    ),
                )
            }
            updateState { next }
        }
    }
}
