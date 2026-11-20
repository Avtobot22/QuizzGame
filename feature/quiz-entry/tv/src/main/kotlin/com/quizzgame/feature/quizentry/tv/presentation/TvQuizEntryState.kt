package com.quizzgame.feature.quizentry.tv.presentation

import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import com.quizzgame.shared.catalog.domain.entity.QuizEntry
import com.quizzgame.shared.room.domain.entity.result.RoomFailure
import com.quizzgame.shared.room.domain.entity.session.RoomId

internal sealed interface TvQuizEntryState {
    data object Loading : TvQuizEntryState
    data class Content(
        val value: QuizEntry,
        val room: TvQuizRoomState = TvQuizRoomState(),
    ) : TvQuizEntryState
    data class Error(val spec: ErrorPresentationSpec) : TvQuizEntryState
}

internal data class TvQuizRoomState(
    val creating: Boolean = false,
    val failure: RoomFailure? = null,
    val activeRoom: RoomId? = null,
    val destination: RoomId? = null,
)
