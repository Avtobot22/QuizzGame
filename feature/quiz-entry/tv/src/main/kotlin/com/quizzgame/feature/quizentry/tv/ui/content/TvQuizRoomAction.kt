package com.quizzgame.feature.quizentry.tv.ui.content

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvActionLabel
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.designsystem.tv.QuizzTvNoticeBanner
import com.quizzgame.core.error.operation.ErrorOperationContext
import com.quizzgame.core.error.operation.OperationKind
import com.quizzgame.core.error.operation.RetrySafety
import com.quizzgame.core.error.ui.common.presentation.errorUiModel
import com.quizzgame.core.error.ui.tv.policy.TvErrorDecisionPolicy
import com.quizzgame.core.ui.status.QuizzNoticeTone
import com.quizzgame.core.ui.status.QuizzStatusIcon
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
    if (!available && state.activeRoom == null) {
        QuizzTvNoticeBanner(tone = QuizzNoticeTone.NEUTRAL, icon = QuizzStatusIcon.CATALOG) {
            Text(stringResource(R.string.quiz_entry_unavailable), style = MaterialTheme.typography.titleSmall)
        }
        return
    }
    val label = when {
        state.activeRoom != null -> RoomR.string.room_resume
        state.creating -> R.string.quiz_entry_creating_room
        onSignIn != null -> R.string.quiz_entry_sign_in
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
        enabled = !state.creating,
    ) { QuizzTvActionLabel(stringResource(label)) }

    val failure = state.failure ?: return
    if (failure.reason == RoomFailureReason.HOST_AUTH_REQUIRED && onSignIn != null) {
        QuizzTvNoticeBanner(icon = QuizzStatusIcon.AUTH) {
            Text(stringResource(R.string.quiz_entry_room_auth_required), style = MaterialTheme.typography.titleSmall)
        }
    } else {
        val context = ErrorOperationContext(OperationKind.USER_COMMAND, true, RetrySafety.SAFE_TO_REPEAT)
        val error = errorUiModel(TvErrorDecisionPolicy().decide(failure.cause, context))
        QuizzTvNoticeBanner(tone = QuizzNoticeTone.ERROR, icon = error.icon) {
            Text(stringResource(R.string.quiz_entry_room_create_failed), style = MaterialTheme.typography.titleSmall)
            Text(
                error.title,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
