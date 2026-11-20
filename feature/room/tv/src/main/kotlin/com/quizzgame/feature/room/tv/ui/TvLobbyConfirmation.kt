package com.quizzgame.feature.room.tv.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import com.quizzgame.core.designsystem.tv.QuizzTvConfirmationDialog
import com.quizzgame.feature.room.tv.R
import com.quizzgame.shared.room.domain.entity.command.RoomCommand
import com.quizzgame.shared.room.domain.entity.snapshot.RoomParticipant

internal sealed interface TvLobbyConfirmation {
    val focus: FocusRequester

    data class Close(override val focus: FocusRequester) : TvLobbyConfirmation
    data class Kick(val player: RoomParticipant, override val focus: FocusRequester) : TvLobbyConfirmation
}

@Composable
internal fun TvLobbyDialog(
    confirmation: TvLobbyConfirmation,
    onDismiss: () -> Unit,
    onCommand: (RoomCommand) -> Unit,
) {
    when (confirmation) {
        is TvLobbyConfirmation.Close -> QuizzTvConfirmationDialog(
            stringResource(R.string.room_close_title),
            stringResource(R.string.room_close_description),
            { onCommand(RoomCommand.Close) },
            onDismiss,
            confirmLabel = stringResource(R.string.room_close_action),
            returnFocusRequester = confirmation.focus,
        )
        is TvLobbyConfirmation.Kick -> QuizzTvConfirmationDialog(
            stringResource(R.string.room_kick_title),
            stringResource(R.string.room_kick_description, confirmation.player.displayName),
            { onCommand(RoomCommand.Kick(confirmation.player.id)) },
            onDismiss,
            confirmLabel = stringResource(R.string.room_kick_action),
            returnFocusRequester = confirmation.focus,
        )
    }
}
