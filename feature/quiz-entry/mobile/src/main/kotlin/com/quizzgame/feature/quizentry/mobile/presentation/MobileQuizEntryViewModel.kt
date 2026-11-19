package com.quizzgame.feature.quizentry.mobile.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.quizzgame.core.error.operation.ErrorOperationContext
import com.quizzgame.core.error.operation.OperationKind
import com.quizzgame.core.error.operation.RetrySafety
import com.quizzgame.core.error.policy.DefaultErrorDecisionPolicy
import com.quizzgame.core.presentation.BaseViewModel
import com.quizzgame.shared.quizentry.domain.entity.QuizEntryResult
import com.quizzgame.shared.quizentry.domain.usecase.LoadQuizEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class MobileQuizEntryViewModel @Inject constructor(
    private val loadQuizEntryUseCase: LoadQuizEntryUseCase,
    savedState: SavedStateHandle,
) : BaseViewModel<MobileQuizEntryState, MobileQuizEntryIntent, Nothing>() {
    private var loading: Job? = null

    private val saved = savedState
    private var entryId: String? = savedState["entryId"]

    init { if (entryId != null) load() }

    override fun createInitialState(): MobileQuizEntryState = MobileQuizEntryState.Loading

    override fun handleIntent(intent: MobileQuizEntryIntent) {
        when (intent) {
            MobileQuizEntryIntent.Connect -> updateState {
                if (it is MobileQuizEntryState.Content && it.value.available) {
                    it.copy(destination = MobileQuizEntryDestination.PLAY)
                } else {
                    it
                }
            }
            MobileQuizEntryIntent.NavigationHandled -> updateState {
                if (it is MobileQuizEntryState.Content) it.copy(destination = null) else it
            }
            MobileQuizEntryIntent.Retry -> if (state.value is MobileQuizEntryState.Error) load()
            is MobileQuizEntryIntent.Load -> if (entryId == null) {
                entryId = intent.entryId
                saved["entryId"] = intent.entryId
                load()
            }
        }
    }

    private fun load() {
        if (loading?.isActive == true) return
        val requestedId = entryId ?: return
        updateState { MobileQuizEntryState.Loading }
        loading = viewModelScope.launch {
            val next = when (val result = loadQuizEntryUseCase(requestedId)) {
                is QuizEntryResult.Loaded -> MobileQuizEntryState.Content(result.content)
                is QuizEntryResult.Failed -> {
                    val spec = DefaultErrorDecisionPolicy().decide(
                        result.failure,
                        ErrorOperationContext(
                            OperationKind.INITIAL_LOAD,
                            false,
                            RetrySafety.SAFE_TO_REPEAT,
                        ),
                    )
                    MobileQuizEntryState.Error(spec)
                }
            }
            updateState { next }
        }
    }
}
