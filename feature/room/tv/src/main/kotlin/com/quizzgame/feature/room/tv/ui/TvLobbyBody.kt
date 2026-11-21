package com.quizzgame.feature.room.tv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import com.quizzgame.feature.room.tv.presentation.TvLobbyState
import com.quizzgame.shared.room.domain.entity.snapshot.RoomParticipant
import com.quizzgame.shared.room.domain.entity.snapshot.RoomPhase

internal const val TV_LOBBY_INVITE_WEIGHT = 0.83f
internal const val TV_LOBBY_ROSTER_WEIGHT = 1.17f

@Composable
internal fun TvLobbyBody(
    state: TvLobbyState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onManage: (RoomParticipant, FocusRequester) -> Unit,
) {
    val room = requireNotNull(state.snapshot)
    val notice = state.notice()
    Row(modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        TvRoomInvite(room.join, Modifier.weight(TV_LOBBY_INVITE_WEIGHT))
        Column(Modifier.weight(TV_LOBBY_ROSTER_WEIGHT), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TvRoomRoster(
                room,
                canManage = state.live && !state.sending && room.permissions.canKick && room.phase == RoomPhase.LOBBY,
                onManage = onManage,
                modifier = Modifier.weight(1f),
            )
            if (notice != null) {
                TvLobbyNotice(notice, onRetry)
            } else {
                TvRoomReadiness(room)
            }
        }
    }
}
