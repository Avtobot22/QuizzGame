package com.quizzgame.feature.room.mobile.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzConfirmationDialog
import com.quizzgame.core.designsystem.mobile.QuizzScreenScaffold
import com.quizzgame.core.designsystem.mobile.QuizzTopBar
import com.quizzgame.feature.room.mobile.R
import com.quizzgame.feature.room.mobile.presentation.MobileLobbyIntent
import com.quizzgame.feature.room.mobile.presentation.MobileLobbyState
import com.quizzgame.feature.room.mobile.ui.tags.MobileLobbyTags
import com.quizzgame.shared.room.domain.entity.session.RoomStreamState
import com.quizzgame.shared.room.R as RoomR

@Composable
internal fun MobileLobbyContent(
    state: MobileLobbyState,
    onBack: () -> Unit,
    onIntent: (MobileLobbyIntent) -> Unit,
) {
    var confirm by rememberSaveable { mutableStateOf(false) }
    val inRoom = state.self != null && state.endReason == null
    val back = { if (inRoom) confirm = true else onBack() }
    val onRetry = { onIntent(MobileLobbyIntent.Retry) }
    val showLobby = !state.gameStarted && (state.snapshot != null || state.stream == RoomStreamState.Connecting)
    BackHandler { back() }
    QuizzScreenScaffold(
        Modifier.testTag(MobileLobbyTags.ROOT),
        topBar = {
            if (showLobby) QuizzTopBar(stringResource(RoomR.string.room_lobby), onBack = back)
        },
    ) { inset ->
        Box(Modifier.fillMaxSize().padding(inset), contentAlignment = Alignment.TopCenter) {
            val modifier = Modifier.widthIn(max = 560.dp).fillMaxSize().padding(horizontal = 20.dp)
            when {
                showLobby && state.snapshot != null -> {
                    MobileLobbyReadyContent(state, onIntent, onRetry, back, modifier)
                }
                showLobby -> MobileLobbySkeleton(modifier)
                else -> MobileLobbyStatus(state, back, onRetry)
            }
        }
    }
    if (confirm && inRoom) {
        QuizzConfirmationDialog(
            stringResource(R.string.room_leave_title),
            stringResource(R.string.room_leave_description),
            {
                confirm = false
                onIntent(MobileLobbyIntent.Leave)
            },
            { confirm = false },
            enabled = !state.sending,
            confirmLabel = stringResource(R.string.room_leave),
        )
    }
}
