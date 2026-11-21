package com.quizzgame.feature.room.mobile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzButton
import com.quizzgame.core.designsystem.mobile.QuizzStatusContent
import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.error.operation.ErrorOperationContext
import com.quizzgame.core.error.operation.OperationKind
import com.quizzgame.core.error.operation.RetrySafety
import com.quizzgame.core.error.policy.DefaultErrorDecisionPolicy
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.ui.common.presentation.errorUiModel
import com.quizzgame.core.ui.status.QuizzNoticeTone
import com.quizzgame.core.ui.status.QuizzStatusIcon
import com.quizzgame.feature.room.mobile.R
import com.quizzgame.feature.room.mobile.ui.tags.MobileLobbyTags
import com.quizzgame.shared.room.domain.entity.session.RoomStreamState
import com.quizzgame.shared.room.presentation.RoomLobbyState
import com.quizzgame.shared.room.presentation.endDescription
import com.quizzgame.shared.room.presentation.endLabel
import com.quizzgame.shared.room.R as RoomR

@Composable
internal fun MobileLobbyStatus(state: RoomLobbyState, onBack: () -> Unit, onRetry: () -> Unit) {
    val reason = state.endReason
    if (reason != null) {
        MobileLobbyStatusPage(
            stringResource(reason.endLabel()),
            stringResource(reason.endDescription()),
            QuizzStatusIcon.ROOM,
            actions = { ReturnToPlay(onBack) },
        )
        return
    }
    if (state.gameStarted) {
        MobileLobbyStatusPage(
            stringResource(RoomR.string.room_started),
            stringResource(R.string.room_started_description),
            QuizzStatusIcon.CATALOG,
        )
        return
    }
    when (val stream = state.stream) {
        is RoomStreamState.Failed -> MobileLobbyFailure(stream.failure.cause, onBack, onRetry)
        else -> MobileLobbyStatusPage(
            stringResource(RoomR.string.room_reconnecting),
            stringResource(RoomR.string.room_reconnecting_description),
            QuizzStatusIcon.NETWORK,
            tone = QuizzNoticeTone.ERROR,
            actions = { ReturnToPlay(onBack) },
        )
    }
}

@Composable
private fun MobileLobbyFailure(failure: AppFailure, onBack: () -> Unit, onRetry: () -> Unit) {
    val context = ErrorOperationContext(OperationKind.INITIAL_LOAD, false, RetrySafety.SAFE_TO_REPEAT)
    val model = errorUiModel(DefaultErrorDecisionPolicy().decide(failure, context))
    val retry = model.actions.firstOrNull { it.kind == ErrorActionKind.RETRY }
    val title = if (failure == AppFailure.InvalidServerData) stringResource(R.string.room_open_failed) else model.title
    val message = model.message ?: stringResource(
        if (retry == null) R.string.room_open_from_play else R.string.room_try_open_again,
    )
    MobileLobbyStatusPage(title, message, model.icon, tone = QuizzNoticeTone.ERROR) {
        if (retry == null) {
            ReturnToPlay(onBack)
        } else {
            QuizzButton(onRetry, Modifier.fillMaxWidth()) { Text(retry.label) }
        }
    }
}

@Composable
private fun ReturnToPlay(onBack: () -> Unit) {
    QuizzButton(onBack, Modifier.fillMaxWidth().testTag(MobileLobbyTags.EXIT)) {
        Text(stringResource(R.string.room_to_play))
    }
}

@Composable
private fun MobileLobbyStatusPage(
    title: String,
    message: String,
    icon: QuizzStatusIcon,
    tone: QuizzNoticeTone = QuizzNoticeTone.NEUTRAL,
    actions: @Composable () -> Unit = {},
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        QuizzStatusContent(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            icon = icon,
            tone = tone,
            actions = actions,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    title,
                    Modifier.semantics { heading() },
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )
                Text(
                    message,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
