package com.quizzgame.feature.room.tv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.designsystem.tv.QuizzTvButtonStyle
import com.quizzgame.core.designsystem.tv.QuizzTvScreenScaffold
import com.quizzgame.core.navigation.focus.TvEntryFocus
import com.quizzgame.core.ui.tokens.QuizzTvLayout
import com.quizzgame.feature.room.tv.R
import com.quizzgame.feature.room.tv.presentation.TvLobbyIntent
import com.quizzgame.feature.room.tv.presentation.TvLobbyState
import com.quizzgame.feature.room.tv.ui.tags.TvLobbyTags
import com.quizzgame.shared.room.domain.entity.session.RoomStreamState
import com.quizzgame.core.ui.R as UiR
import com.quizzgame.shared.room.R as RoomR

@Composable
internal fun TvLobbyContent(
    state: TvLobbyState,
    onBack: () -> Unit,
    onIntent: (TvLobbyIntent) -> Unit,
) {
    val returnFocus = remember { FocusRequester() }
    val closeFocus = remember { FocusRequester() }
    val onRetry = { onIntent(TvLobbyIntent.Retry) }
    val showLobby = !state.gameStarted && (state.snapshot != null || state.stream == RoomStreamState.Connecting)
    var confirmation by remember { mutableStateOf<TvLobbyConfirmation?>(null) }
    QuizzTvScreenScaffold(Modifier.testTag(TvLobbyTags.ROOT)) {
        if (showLobby) {
            Column(
                Modifier.fillMaxSize().padding(
                    horizontal = QuizzTvLayout.screenInset.dp,
                    vertical = QuizzTvLayout.verticalInset.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                TvLobbyTopBar(state, onBack, returnFocus, closeFocus) {
                    confirmation = TvLobbyConfirmation.Close(closeFocus)
                }
                if (state.snapshot == null) {
                    TvLobbySkeleton(Modifier.weight(1f))
                } else {
                    TvLobbyBody(state, onRetry, Modifier.weight(1f)) { player, focus ->
                        confirmation = TvLobbyConfirmation.Kick(player, focus)
                    }
                }
            }
            TvEntryFocus(returnFocus)
        } else {
            TvLobbyStatus(state, onBack, onRetry)
        }
    }
    val selected = confirmation
    val canConfirm = selected?.allowedBy(state) == true
    LaunchedEffect(selected, canConfirm) {
        if (selected != null && !canConfirm) {
            confirmation = null
            if (showLobby) returnFocus.requestFocus()
        }
    }
    if (selected != null && canConfirm) {
        TvLobbyDialog(selected, onDismiss = { confirmation = null }) { command ->
            confirmation = null
            if (showLobby) returnFocus.requestFocus()
            onIntent(command)
        }
    }
}

@Composable
private fun TvLobbyTopBar(
    state: TvLobbyState,
    onBack: () -> Unit,
    backFocus: FocusRequester,
    closeFocus: FocusRequester,
    onClose: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        QuizzTvButton(onBack, Modifier.focusRequester(backFocus), style = QuizzTvButtonStyle.QUIET) {
            Icon(
                painterResource(UiR.drawable.ic_quizz_back),
                stringResource(RoomR.string.room_back),
                Modifier.size(24.dp),
            )
        }
        Text(
            state.snapshot?.entry?.title ?: stringResource(RoomR.string.room_lobby),
            Modifier.weight(1f).semantics { heading() },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.headlineMedium,
        )
        if (state.snapshot?.permissions?.canClose == true) {
            QuizzTvButton(
                onClose,
                Modifier.focusRequester(closeFocus).testTag(TvLobbyTags.EXIT),
                style = QuizzTvButtonStyle.SECONDARY,
                enabled = !state.sending,
            ) { Text(stringResource(R.string.room_close_action)) }
        }
    }
}
