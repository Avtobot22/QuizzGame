package com.quizzgame.feature.dashboard.tv.presentation

import androidx.lifecycle.viewModelScope
import com.quizzgame.core.error.operation.ErrorOperationContext
import com.quizzgame.core.error.operation.OperationKind
import com.quizzgame.core.error.operation.RetrySafety
import com.quizzgame.core.error.ui.tv.policy.TvErrorDecisionPolicy
import com.quizzgame.core.presentation.BaseViewModel
import com.quizzgame.feature.dashboard.tv.domain.LoadDashboardUseCase
import com.quizzgame.feature.dashboard.tv.domain.entity.DashboardResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class TvDashboardViewModel @Inject constructor(
    private val loadDashboardUseCase: LoadDashboardUseCase,
) : BaseViewModel<TvDashboardState, TvDashboardIntent, Nothing>() {
    private var loading: Job? = null

    init { load() }

    override fun createInitialState(): TvDashboardState = TvDashboardState.Loading

    override fun handleIntent(intent: TvDashboardIntent) {
        when (intent) {
            TvDashboardIntent.Retry -> {
                val current = state.value
                if (current is TvDashboardState.Error || current == TvDashboardState.Empty) load()
            }
        }
    }

    private fun load() {
        if (loading?.isActive == true) return
        updateState { TvDashboardState.Loading }
        loading = viewModelScope.launch {
            val next = when (val result = loadDashboardUseCase()) {
                is DashboardResult.Loaded -> if (result.content.tabs.none { it.enabled }) {
                    TvDashboardState.Empty
                } else {
                    TvDashboardState.Content(result.content)
                }
                is DashboardResult.Failed -> TvDashboardState.Error(
                    TvErrorDecisionPolicy().decide(
                        result.failure,
                        ErrorOperationContext(
                            OperationKind.INITIAL_LOAD,
                            hasUsableContent = false,
                            RetrySafety.SAFE_TO_REPEAT,
                        ),
                    ),
                )
            }
            updateState { next }
        }
    }
}
