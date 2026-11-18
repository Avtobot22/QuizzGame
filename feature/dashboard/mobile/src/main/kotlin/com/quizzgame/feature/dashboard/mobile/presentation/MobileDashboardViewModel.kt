package com.quizzgame.feature.dashboard.mobile.presentation

import androidx.lifecycle.viewModelScope
import com.quizzgame.core.error.operation.ErrorOperationContext
import com.quizzgame.core.error.operation.OperationKind
import com.quizzgame.core.error.operation.RetrySafety
import com.quizzgame.core.error.policy.DefaultErrorDecisionPolicy
import com.quizzgame.core.presentation.BaseViewModel
import com.quizzgame.feature.dashboard.mobile.domain.LoadDashboardUseCase
import com.quizzgame.feature.dashboard.mobile.domain.entity.DashboardResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class MobileDashboardViewModel @Inject constructor(private val load: LoadDashboardUseCase) :
    BaseViewModel<MobileDashboardState, MobileDashboardIntent, Nothing>() {
    private var loading: Job? = null
    init { refresh() }
    override fun createInitialState(): MobileDashboardState = MobileDashboardState.Bootstrapped()
    override fun handleIntent(intent: MobileDashboardIntent) {
        when (intent) {
            MobileDashboardIntent.Retry -> refresh()
            MobileDashboardIntent.DismissError -> updateState { MobileDashboardState.Bootstrapped(it.dashboard) }
        }
    }
    private fun refresh() {
        if (loading?.isActive == true) return
        loading = viewModelScope.launch {
            val next = when (val result = load()) {
                is DashboardResult.Loaded -> MobileDashboardState.Remote(result.content)
                is DashboardResult.Failed -> MobileDashboardState.FailureWithFallback(
                    DefaultErrorDecisionPolicy().decide(
                        result.failure,
                        ErrorOperationContext(OperationKind.INITIAL_LOAD, true, RetrySafety.SAFE_TO_REPEAT),
                    ),
                )
            }
            updateState { next }
        }
    }
}
