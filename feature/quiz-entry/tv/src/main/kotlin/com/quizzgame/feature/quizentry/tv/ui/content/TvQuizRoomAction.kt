package com.quizzgame.feature.quizentry.tv.ui.content

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvActionLabel
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.error.operation.ErrorOperationContext
import com.quizzgame.core.error.operation.OperationKind
import com.quizzgame.core.error.operation.RetrySafety
import com.quizzgame.core.error.presentation.ErrorSurface
import com.quizzgame.core.error.ui.tv.components.QuizzTvErrorInline
import com.quizzgame.core.error.ui.tv.policy.TvErrorDecisionPolicy
import com.quizzgame.feature.quizentry.tv.R
import com.quizzgame.feature.quizentry.tv.presentation.TvQuizEntryIntent
import com.quizzgame.feature.quizentry.tv.presentation.TvQuizRoomState
import com.quizzgame.feature.quizentry.tv.ui.tags.TvQuizEntryTags
import com.quizzgame.shared.room.domain.entity.result.RoomFailureReason
import com.quizzgame.shared.room.R as RoomR

@Composable
internal fun TvQuizRoomAction(
    available: Boolean,
    state: TvQuizRoomState,
    onIntent: (TvQuizEntryIntent) -> Unit,
    onSignIn: (() -> Unit)?,
) {
    val label = when {
        state.activeRoom != null -> RoomR.string.room_resume
        state.creating -> R.string.quiz_entry_creating_room
        available && onSignIn != null -> R.string.quiz_entry_sign_in
        else -> R.string.quiz_entry_create_room
    }
    QuizzTvButton(
        onClick = {
            when {
                state.activeRoom != null -> onIntent(TvQuizEntryIntent.ResumeRoom)
                onSignIn != null -> onSignIn()
                else -> onIntent(TvQuizEntryIntent.CreateRoom)
            }
        },
        modifier = Modifier.testTag(TvQuizEntryTags.CREATE_ROOM),
        enabled = !state.creating && (available || state.activeRoom != null),
    ) { QuizzTvActionLabel(stringResource(label)) }

    val failure = state.failure ?: return
    if (failure.reason == RoomFailureReason.HOST_AUTH_REQUIRED && onSignIn != null) {
        Text(stringResource(R.string.quiz_entry_room_auth_required), style = MaterialTheme.typography.bodyMedium)
    } else {
        val context = ErrorOperationContext(OperationKind.USER_COMMAND, true, RetrySafety.SAFE_TO_REPEAT)
        val spec = TvErrorDecisionPolicy().decide(failure.cause, context)
        QuizzTvErrorInline(spec.copy(surface = ErrorSurface.INLINE, actions = emptyList()), {})
    }
}
