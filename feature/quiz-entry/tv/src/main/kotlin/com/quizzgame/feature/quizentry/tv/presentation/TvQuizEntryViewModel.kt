package com.quizzgame.feature.quizentry.tv.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.quizzgame.core.error.operation.ErrorOperationContext
import com.quizzgame.core.error.operation.OperationKind
import com.quizzgame.core.error.operation.RetrySafety
import com.quizzgame.core.error.ui.tv.policy.TvErrorDecisionPolicy
import com.quizzgame.core.presentation.BaseViewModel
import com.quizzgame.shared.quizentry.domain.entity.QuizEntryResult
import com.quizzgame.shared.quizentry.domain.usecase.LoadQuizEntryUseCase
import com.quizzgame.shared.room.domain.entity.result.RoomResult
import com.quizzgame.shared.room.domain.usecase.CreateRoomUseCase
import com.quizzgame.shared.room.domain.usecase.ObserveActiveRoomUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class TvQuizEntryViewModel @Inject constructor(
    private val loadQuizEntryUseCase: LoadQuizEntryUseCase,
    savedState: SavedStateHandle,
    private val createRoomUseCase: CreateRoomUseCase,
    private val observeActiveRoomUseCase: ObserveActiveRoomUseCase,
) : BaseViewModel<TvQuizEntryState, TvQuizEntryIntent, Nothing>() {
    private var loading: Job? = null

    private val saved = savedState
    private var entryId: String? = savedState["entryId"]
    private var roomState = TvQuizRoomState()

    init {
        if (entryId != null) load()
        viewModelScope.launch {
            observeActiveRoomUseCase().collect { result ->
                when (result) {
                    is RoomResult.Failed -> updateRoom { it.copy(failure = result.failure) }
                    is RoomResult.Success -> updateRoom { it.copy(activeRoom = result.value?.roomId) }
                }
            }
        }
    }

    override fun createInitialState(): TvQuizEntryState = TvQuizEntryState.Loading

    override fun handleIntent(intent: TvQuizEntryIntent) {
        when (intent) {
            TvQuizEntryIntent.CreateRoom -> createRoom()
            TvQuizEntryIntent.ResumeRoom -> updateRoom { it.copy(destination = it.activeRoom) }
            TvQuizEntryIntent.RoomOpened -> updateRoom { it.copy(destination = null) }
            TvQuizEntryIntent.Retry -> if (state.value is TvQuizEntryState.Error) load()
            is TvQuizEntryIntent.Load -> if (entryId == null) {
                entryId = intent.entryId
                saved["entryId"] = intent.entryId
                load()
            }
        }
    }

    private fun createRoom() {
        val current = state.value as? TvQuizEntryState.Content ?: return
        val id = entryId ?: return
        if (!current.value.available || current.room.creating || current.room.activeRoom != null) return
        updateRoom { it.copy(creating = true, failure = null) }
        viewModelScope.launch {
            when (val result = createRoomUseCase(id)) {
                is RoomResult.Success -> {
                    updateRoom { it.copy(creating = false, destination = result.value.roomId) }
                }
                is RoomResult.Failed -> {
                    updateRoom { it.copy(creating = false, failure = result.failure) }
                }
            }
        }
    }

    private fun updateRoom(transform: (TvQuizRoomState) -> TvQuizRoomState) {
        val next = transform(roomState)
        roomState = next
        updateState { if (it is TvQuizEntryState.Content) it.copy(room = next) else it }
    }

    private fun load() {
        if (loading?.isActive == true) return
        val requestedId = entryId ?: return
        updateState { TvQuizEntryState.Loading }
        loading = viewModelScope.launch {
            val next = when (val result = loadQuizEntryUseCase(requestedId)) {
                is QuizEntryResult.Loaded -> TvQuizEntryState.Content(
                    result.content,
                    roomState,
                )
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
