package com.quizzgame.feature.room.tv.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvNoticeBanner
import com.quizzgame.core.error.operation.ErrorOperationContext
import com.quizzgame.core.error.operation.OperationKind
import com.quizzgame.core.error.operation.RetrySafety
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.ui.tv.components.QuizzTvErrorBanner
import com.quizzgame.core.error.ui.tv.policy.TvErrorDecisionPolicy
import com.quizzgame.core.ui.status.QuizzNoticeTone
import com.quizzgame.core.ui.status.QuizzStatusIcon
import com.quizzgame.shared.room.domain.entity.result.RoomFailureReason
import com.quizzgame.shared.room.domain.entity.session.RoomStreamState
import com.quizzgame.shared.room.presentation.RoomLobbyState
import com.quizzgame.shared.room.R as RoomR

@Composable
internal fun TvLobbyNotice(state: RoomLobbyState, onRetry: () -> Unit) {
    val error = state.error
    val label = when (error?.reason) {
        RoomFailureReason.STATE_CONFLICT, RoomFailureReason.VERSION_CONFLICT -> RoomR.string.room_conflict
        RoomFailureReason.FORBIDDEN -> RoomR.string.room_action_denied
        else -> RoomR.string.room_reconnecting.takeIf { state.stream is RoomStreamState.Reconnecting }
    }
    if (label != null) {
        val reconnecting = state.stream is RoomStreamState.Reconnecting
        val icon = if (reconnecting) QuizzStatusIcon.NETWORK else QuizzStatusIcon.GENERIC
        QuizzTvNoticeBanner(tone = QuizzNoticeTone.ERROR, icon = icon) {
            Text(stringResource(label), style = MaterialTheme.typography.bodyMedium)
        }
    } else if (error != null) {
        val retry = if (error.uncertain) RetrySafety.SAFE_TO_REPEAT else RetrySafety.FORBIDDEN
        val context = ErrorOperationContext(OperationKind.USER_COMMAND, true, retry)
        val spec = TvErrorDecisionPolicy().decide(error.cause, context)
        QuizzTvErrorBanner(spec.copy(actions = spec.actions.filter { it == ErrorActionKind.RETRY }), { onRetry() })
    }
}
