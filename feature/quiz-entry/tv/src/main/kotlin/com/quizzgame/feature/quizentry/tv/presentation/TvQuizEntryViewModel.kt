package com.quizzgame.feature.quizentry.tv.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.quizzgame.core.error.operation.ErrorOperationContext
import com.quizzgame.core.error.operation.OperationKind
import com.quizzgame.core.error.operation.RetrySafety
import com.quizzgame.core.error.ui.tv.policy.TvErrorDecisionPolicy
import com.quizzgame.core.presentation.BaseViewModel
import com.quizzgame.shared.quizentry.domain.LoadQuizEntryUseCase
import com.quizzgame.shared.quizentry.domain.entity.QuizEntryResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class TvQuizEntryViewModel @Inject constructor(
    private val loadQuizEntryUseCase: LoadQuizEntryUseCase,
    savedState: SavedStateHandle,
) : BaseViewModel<TvQuizEntryState, TvQuizEntryIntent, Nothing>() {
    private var loading: Job? = null

    private val saved = savedState
    private var entryId: String? = savedState["entryId"]

    init { if (entryId != null) load() }

    override fun createInitialState(): TvQuizEntryState = TvQuizEntryState.Loading

    override fun handleIntent(intent: TvQuizEntryIntent) {
        when (intent) {
            TvQuizEntryIntent.Retry -> if (state.value is TvQuizEntryState.Error) load()
            is TvQuizEntryIntent.Load -> if (entryId == null) {
                entryId = intent.entryId
                saved["entryId"] = intent.entryId
                load()
            }
        }
    }

    private fun load() {
        if (loading?.isActive == true) return
        val requestedId = entryId ?: return
        updateState { TvQuizEntryState.Loading }
        loading = viewModelScope.launch {
            val next = when (val result = loadQuizEntryUseCase(requestedId)) {
                is QuizEntryResult.Loaded -> TvQuizEntryState.Content(result.content)
                is QuizEntryResult.Failed -> {
                    val spec = TvErrorDecisionPolicy().decide(
                        result.failure,
                        ErrorOperationContext(
                            OperationKind.INITIAL_LOAD,
                            false,
                            RetrySafety.SAFE_TO_REPEAT,
                        ),
                    )
                    TvQuizEntryState.Error(spec)
                }
            }
            updateState { next }
        }
    }
}
