package com.quizzgame.feature.room.tv.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import com.quizzgame.core.designsystem.tv.QuizzTvConfirmationDialog
import com.quizzgame.feature.room.tv.R
import com.quizzgame.feature.room.tv.presentation.TvLobbyIntent
import com.quizzgame.feature.room.tv.presentation.TvLobbyState
import com.quizzgame.shared.room.domain.entity.snapshot.RoomParticipant
import com.quizzgame.shared.room.domain.entity.snapshot.RoomPhase

internal sealed interface TvLobbyConfirmation {
    val focus: FocusRequester

    data class Close(override val focus: FocusRequester) : TvLobbyConfirmation
    data class Kick(val player: RoomParticipant, override val focus: FocusRequester) : TvLobbyConfirmation
}

internal fun TvLobbyConfirmation.allowedBy(state: TvLobbyState): Boolean {
    val room = state.snapshot ?: return false
    return when (this) {
        is TvLobbyConfirmation.Close -> room.permissions.canClose && !state.gameStarted
        is TvLobbyConfirmation.Kick ->
            state.live && room.phase == RoomPhase.LOBBY && room.permissions.canKick &&
                room.players.any { it.id == player.id }
    }
}

@Composable
internal fun TvLobbyDialog(
    confirmation: TvLobbyConfirmation,
    onDismiss: () -> Unit,
    onCommand: (TvLobbyIntent) -> Unit,
) {
    when (confirmation) {
        is TvLobbyConfirmation.Close -> QuizzTvConfirmationDialog(
            stringResource(R.string.room_close_title),
            stringResource(R.string.room_close_description),
            { onCommand(TvLobbyIntent.Close) },
            onDismiss,
            confirmLabel = stringResource(R.string.room_close_action),
            returnFocusRequester = confirmation.focus,
        )
        is TvLobbyConfirmation.Kick -> QuizzTvConfirmationDialog(
            stringResource(R.string.room_kick_title),
            stringResource(R.string.room_kick_description, confirmation.player.displayName),
            { onCommand(TvLobbyIntent.Kick(confirmation.player.id)) },
            onDismiss,
            confirmLabel = stringResource(R.string.room_kick_action),
            returnFocusRequester = confirmation.focus,
        )
    }
}
