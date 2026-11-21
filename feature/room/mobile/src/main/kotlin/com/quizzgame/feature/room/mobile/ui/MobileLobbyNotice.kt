package com.quizzgame.feature.room.mobile.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzNoticeBanner
import com.quizzgame.core.ui.status.QuizzStatusIcon
import com.quizzgame.feature.room.mobile.presentation.MobileLobbyIntent
import com.quizzgame.feature.room.mobile.presentation.MobileLobbyState
import com.quizzgame.shared.room.R
import com.quizzgame.shared.room.presentation.RoomLobbyNotice
import com.quizzgame.shared.room.presentation.commandFailureNotice
import com.quizzgame.shared.room.presentation.reconnectingNotice

@Composable
internal fun MobileLobbyNotice(notice: RoomLobbyNotice, onRetry: () -> Unit) {
    QuizzNoticeBanner(
        tone = notice.tone,
        icon = notice.icon,
        trailing = if (notice.retry) ({ RetryIcon(onRetry) }) else null,
    ) {
        Text(stringResource(notice.title), style = MaterialTheme.typography.titleSmall)
        notice.message?.let {
            Text(
                stringResource(it),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun RetryIcon(onRetry: () -> Unit) {
    IconButton(onRetry) {
        Icon(
            painterResource(QuizzStatusIcon.SYNC.resource),
            contentDescription = stringResource(R.string.room_retry),
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

internal fun MobileLobbyState.notice(): RoomLobbyNotice? {
    val error = commandError ?: return reconnectingNotice(stream)
    val (title, icon) = when (error.command) {
        is MobileLobbyIntent.SetReady -> R.string.room_ready_failed to QuizzStatusIcon.CHECK
        MobileLobbyIntent.Leave        -> R.string.room_leave_failed to QuizzStatusIcon.EXIT
    }
    return commandFailureNotice(stream, error.failure, title, icon, live || error.command == MobileLobbyIntent.Leave)
}
