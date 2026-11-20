package com.quizzgame.feature.room.tv.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.designsystem.tv.QuizzTvButtonStyle
import com.quizzgame.core.designsystem.tv.QuizzTvStatusContent
import com.quizzgame.core.designsystem.tv.QuizzTvStatusCopy
import com.quizzgame.core.error.operation.ErrorOperationContext
import com.quizzgame.core.error.operation.OperationKind
import com.quizzgame.core.error.operation.RetrySafety
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.ui.tv.components.QuizzTvErrorFullScreen
import com.quizzgame.core.error.ui.tv.policy.TvErrorDecisionPolicy
import com.quizzgame.core.navigation.focus.TvEntryFocus
import com.quizzgame.core.ui.status.QuizzNoticeTone
import com.quizzgame.core.ui.status.QuizzStatusIcon
import com.quizzgame.feature.room.tv.R
import com.quizzgame.shared.room.domain.entity.result.RoomFailureReason
import com.quizzgame.shared.room.domain.entity.session.RoomStreamState
import com.quizzgame.shared.room.presentation.RoomLobbyState
import com.quizzgame.shared.room.presentation.endDescription
import com.quizzgame.shared.room.presentation.endLabel
import com.quizzgame.shared.room.R as RoomR

@Composable
internal fun TvLobbyStatus(state: RoomLobbyState, onBack: () -> Unit, onRetry: () -> Unit) {
    val reason = state.endReason
    if (reason != null) {
        TvLobbyStatusPage(
            stringResource(reason.endLabel()),
            stringResource(reason.hostEndDescription()),
            QuizzStatusIcon.ROOM,
            onBack,
        )
        return
    }
    if (state.gameStarted) {
        TvLobbyStatusPage(
            stringResource(RoomR.string.room_started),
            stringResource(R.string.room_started_description),
            QuizzStatusIcon.CATALOG,
        )
        return
    }
    when (val stream = state.stream) {
        is RoomStreamState.Failed -> {
            val context = ErrorOperationContext(OperationKind.INITIAL_LOAD, false, RetrySafety.SAFE_TO_REPEAT)
            val spec = TvErrorDecisionPolicy().decide(stream.failure.cause, context)
            QuizzTvErrorFullScreen(
                spec.copy(actions = spec.actions.filter { it == ErrorActionKind.RETRY } + ErrorActionKind.GO_BACK),
                onAction = { if (it == ErrorActionKind.RETRY) onRetry() else onBack() },
            )
        }
        else -> TvLobbyStatusPage(
            stringResource(RoomR.string.room_reconnecting),
            stringResource(RoomR.string.room_reconnecting_description),
            QuizzStatusIcon.NETWORK,
            onBack,
            tone = QuizzNoticeTone.ERROR,
        )
    }
}

@Composable
private fun TvLobbyStatusPage(
    title: String,
    message: String,
    icon: QuizzStatusIcon,
    onBack: (() -> Unit)? = null,
    tone: QuizzNoticeTone = QuizzNoticeTone.NEUTRAL,
) {
    val focus = remember { FocusRequester() }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        QuizzTvStatusContent(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            icon = icon,
            tone = tone,
            actions = {
                if (onBack != null) {
                    QuizzTvButton(onBack, Modifier.focusRequester(focus), style = QuizzTvButtonStyle.SECONDARY) {
                        Text(stringResource(RoomR.string.room_back))
                    }
                }
            },
        ) { QuizzTvStatusCopy(title, message) }
    }
    TvEntryFocus(focus, requested = onBack != null)
}

@StringRes
private fun RoomFailureReason.hostEndDescription(): Int = when (this) {
    RoomFailureReason.EXPIRED, RoomFailureReason.INVALID_TOKEN, RoomFailureReason.CLOSED -> R.string.room_create_again
    RoomFailureReason.ACCOUNT_CHANGED -> R.string.room_host_account_changed
    else -> endDescription()
}
