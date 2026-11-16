package com.quizzgame.feature.splash.tv.presentation

import androidx.lifecycle.viewModelScope
import com.quizzgame.core.error.operation.ErrorOperationContext
import com.quizzgame.core.error.operation.OperationKind
import com.quizzgame.core.error.operation.RetrySafety
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import com.quizzgame.core.error.ui.tv.policy.TvErrorDecisionPolicy
import com.quizzgame.core.presentation.BaseViewModel
import com.quizzgame.feature.splash.tv.navigation.TvSplashDestination
import com.quizzgame.shared.splash.domain.StartSplashUseCase
import com.quizzgame.shared.splash.domain.entity.SplashResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class TvSplashViewModel @Inject constructor(private val startSplashUseCase: StartSplashUseCase) :
    BaseViewModel<TvSplashUiState, TvSplashIntent, Nothing>() {
    private var job: Job? = null

    init { start() }

    override fun createInitialState(): TvSplashUiState = TvSplashUiState.Loading

    override fun handleIntent(intent: TvSplashIntent) {
        when (intent) {
            TvSplashIntent.Retry             -> if (state.value is TvSplashUiState.Error) start()
            TvSplashIntent.NavigationHandled -> updateState { current ->
                if (current is TvSplashUiState.NavigationPending) {
                    TvSplashUiState.NavigationComplete
                } else {
                    current
                }
            }

            TvSplashIntent.SignIn            -> if (state.value is TvSplashUiState.Error) {
                updateState { TvSplashUiState.NavigationPending(TvSplashDestination.SIGN_IN) }
            }
        }
    }

    private fun start() {
        if (job?.isActive == true) return
        updateState { TvSplashUiState.Loading }
        job = viewModelScope.launch {
            val next = when (val result = startSplashUseCase()) {
                SplashResult.Continue  -> TvSplashUiState.NavigationPending(TvSplashDestination.DASHBOARD)
                SplashResult.SignIn    -> TvSplashUiState.NavigationPending(TvSplashDestination.SIGN_IN)
                is SplashResult.Failed -> TvSplashUiState.Error(
                    TvErrorDecisionPolicy().decide(
                        result.failure,
                        ErrorOperationContext(OperationKind.INITIAL_LOAD, false, RetrySafety.SAFE_TO_REPEAT),
                    ).withExit(),
                )
            }
            updateState { next }
        }
    }
}

private fun ErrorPresentationSpec.withExit(): ErrorPresentationSpec = copy(actions = actions + ErrorActionKind.EXIT)
