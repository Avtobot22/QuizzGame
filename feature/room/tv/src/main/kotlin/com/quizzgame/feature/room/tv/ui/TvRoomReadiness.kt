package com.quizzgame.feature.room.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.feature.room.tv.R
import com.quizzgame.shared.room.domain.entity.snapshot.RoomPhase
import com.quizzgame.shared.room.domain.entity.snapshot.RoomSnapshot
import com.quizzgame.core.ui.R as UiR
import com.quizzgame.shared.room.R as RoomR

internal val TvLobbyReadinessHeight = 60.dp

@Composable
internal fun TvRoomReadiness(room: RoomSnapshot) {
    val missing = room.capacity.minPlayers - room.players.size
    val waiting = room.policy.allPlayersReadyRequired && room.players.any { !it.ready }
    val canStart = room.phase == RoomPhase.LOBBY && missing <= 0 && !waiting
    val label = when {
        room.phase == RoomPhase.STARTING -> stringResource(RoomR.string.room_starting)
        room.phase == RoomPhase.IN_GAME -> stringResource(RoomR.string.room_started)
        room.phase == RoomPhase.CLOSED -> stringResource(RoomR.string.room_closed)
        missing > 0 -> pluralStringResource(R.plurals.room_more_players, missing, missing)
        waiting -> stringResource(R.string.room_start_wait_ready)
        room.policy.allPlayersReadyRequired -> stringResource(R.string.room_all_ready)
        else -> stringResource(R.string.room_enough_players)
    }
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().heightIn(min = TvLobbyReadinessHeight)
            .background(colors.surfaceVariant.copy(alpha = 0.45f), MaterialTheme.shapes.large)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(34.dp).background(colors.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(if (canStart) UiR.drawable.ic_quizz_check else UiR.drawable.ic_quizz_clock),
                null,
                Modifier.size(18.dp),
                tint = colors.onPrimaryContainer,
            )
        }
        Text(label, style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
    }
}
