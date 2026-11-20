package com.quizzgame.feature.room.mobile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzButton
import com.quizzgame.core.designsystem.mobile.QuizzButtonStyle
import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.error.operation.ErrorOperationContext
import com.quizzgame.core.error.operation.OperationKind
import com.quizzgame.core.error.operation.RetrySafety
import com.quizzgame.core.error.policy.DefaultErrorDecisionPolicy
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.ui.common.presentation.errorUiModel
import com.quizzgame.core.ui.status.QuizzStatusIcon
import com.quizzgame.feature.room.mobile.R
import com.quizzgame.shared.room.domain.entity.result.RoomFailureReason
import com.quizzgame.shared.room.domain.entity.session.RoomStreamState
import com.quizzgame.shared.room.presentation.RoomLobbyState
import com.quizzgame.shared.room.R as RoomR

@Composable
internal fun MobileLobbyNotice(state: RoomLobbyState, onRetry: () -> Unit) {
    val error = state.error
    if (error == null) {
        if (state.stream is RoomStreamState.Reconnecting) {
            NoticeStrip(stringResource(RoomR.string.room_reconnecting), QuizzStatusIcon.NETWORK)
        }
        return
    }
    val label = when (error.reason) {
        RoomFailureReason.STATE_CONFLICT, RoomFailureReason.VERSION_CONFLICT -> RoomR.string.room_conflict
        RoomFailureReason.FORBIDDEN -> RoomR.string.room_action_denied
        else -> R.string.room_update_failed.takeIf { error.cause == AppFailure.InvalidServerData }
    }
    if (label != null) {
        NoticeStrip(stringResource(label), QuizzStatusIcon.GENERIC)
    } else {
        val retry = if (error.uncertain) RetrySafety.SAFE_TO_REPEAT else RetrySafety.FORBIDDEN
        val context = ErrorOperationContext(OperationKind.USER_COMMAND, true, retry)
        val model = errorUiModel(DefaultErrorDecisionPolicy().decide(error.cause, context))
        val action = model.actions.firstOrNull { it.kind == ErrorActionKind.RETRY }
        NoticeStrip(model.title, model.icon) {
            if (action != null) {
                QuizzButton(onRetry, style = QuizzButtonStyle.TEXT) { Text(action.label) }
            }
        }
    }
}

@Composable
private fun NoticeStrip(
    message: String,
    icon: QuizzStatusIcon,
    action: @Composable () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().background(colors.surfaceContainerHigh, MaterialTheme.shapes.medium)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            painterResource(icon.resource),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = colors.error,
        )
        Column(Modifier.weight(1f)) {
            Text(message, color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            action()
        }
    }
}
