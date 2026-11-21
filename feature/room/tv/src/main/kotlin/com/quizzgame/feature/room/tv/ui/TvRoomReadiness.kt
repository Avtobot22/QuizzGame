package com.quizzgame.feature.room.tv.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvNoticeBanner
import com.quizzgame.core.ui.status.QuizzNoticeTone
import com.quizzgame.core.ui.status.QuizzStatusIcon
import com.quizzgame.feature.room.tv.R
import com.quizzgame.shared.room.domain.entity.snapshot.RoomConnection
import com.quizzgame.shared.room.domain.entity.snapshot.RoomPhase
import com.quizzgame.shared.room.domain.entity.snapshot.RoomSnapshot
import com.quizzgame.shared.room.R as RoomR

internal val TvLobbyReadinessHeight = 60.dp

@Composable
internal fun TvRoomReadiness(room: RoomSnapshot) {
    val missing = room.capacity.minPlayers - room.players.size
    val waiting = room.policy.allPlayersReadyRequired &&
        room.players.any { !it.ready || it.connection != RoomConnection.CONNECTED }
    val canStart = room.phase == RoomPhase.LOBBY && missing <= 0 && !waiting
    val icon = when {
        canStart -> QuizzStatusIcon.CHECK
        missing > 0 -> QuizzStatusIcon.PEOPLE
        else -> QuizzStatusIcon.CLOCK
    }
    QuizzTvNoticeBanner(tone = if (canStart) QuizzNoticeTone.INFORMATION else QuizzNoticeTone.NEUTRAL, icon = icon) {
        Text(readinessLabel(room, missing, waiting), style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun readinessLabel(room: RoomSnapshot, missing: Int, waiting: Boolean): String = when {
    room.phase == RoomPhase.STARTING -> stringResource(RoomR.string.room_starting)
    room.phase == RoomPhase.IN_GAME -> stringResource(RoomR.string.room_started)
    room.phase == RoomPhase.CLOSED -> stringResource(RoomR.string.room_closed)
    missing > 0 -> pluralStringResource(R.plurals.room_more_players, missing, missing)
    waiting -> stringResource(R.string.room_start_wait_ready)
    room.policy.allPlayersReadyRequired -> stringResource(R.string.room_all_ready)
    else -> stringResource(R.string.room_enough_players)
}
