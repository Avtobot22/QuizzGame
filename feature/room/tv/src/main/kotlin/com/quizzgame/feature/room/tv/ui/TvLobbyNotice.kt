package com.quizzgame.feature.room.tv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvActionLabel
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.designsystem.tv.QuizzTvButtonStyle
import com.quizzgame.core.designsystem.tv.QuizzTvNoticeBanner
import com.quizzgame.core.ui.QuizzIconLabel
import com.quizzgame.core.ui.status.QuizzStatusIcon
import com.quizzgame.feature.room.tv.presentation.TvLobbyIntent
import com.quizzgame.feature.room.tv.presentation.TvLobbyState
import com.quizzgame.feature.room.tv.ui.tags.TvLobbyTags
import com.quizzgame.shared.room.R
import com.quizzgame.shared.room.presentation.RoomLobbyNotice
import com.quizzgame.shared.room.presentation.commandFailureNotice
import com.quizzgame.shared.room.presentation.reconnectingNotice

@Composable
internal fun TvLobbyNotice(notice: RoomLobbyNotice, onRetry: () -> Unit) {
    QuizzTvNoticeBanner(tone = notice.tone, icon = notice.icon) {
        if (notice.retry) {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                if (maxWidth >= 380.dp && LocalDensity.current.fontScale <= 1.25f) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        NoticeCopy(notice, Modifier.weight(1f))
                        RetryAction(onRetry)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        NoticeCopy(notice)
                        RetryAction(onRetry)
                    }
                }
            }
        } else {
            NoticeCopy(notice)
        }
    }
}

@Composable
private fun NoticeCopy(notice: RoomLobbyNotice, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(notice.title), style = MaterialTheme.typography.titleSmall)
        notice.message?.let {
            Text(
                stringResource(it),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun RetryAction(onRetry: () -> Unit) {
    QuizzTvButton(onRetry, modifier = Modifier.testTag(TvLobbyTags.RETRY), style = QuizzTvButtonStyle.QUIET) {
        QuizzIconLabel(QuizzStatusIcon.SYNC.resource, iconSize = 18.dp, tint = LocalContentColor.current) {
            QuizzTvActionLabel(stringResource(R.string.room_retry))
        }
    }
}

internal fun TvLobbyState.notice(): RoomLobbyNotice? {
    val error = commandError ?: return reconnectingNotice(stream)
    val (title, icon) = when (error.command) {
        is TvLobbyIntent.Kick -> R.string.room_kick_failed to QuizzStatusIcon.REMOVE_PLAYER
        TvLobbyIntent.Close   -> R.string.room_close_failed to QuizzStatusIcon.EXIT
    }
    return commandFailureNotice(stream, error.failure, title, icon, live || error.command == TvLobbyIntent.Close)
}
