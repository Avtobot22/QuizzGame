package com.quizzgame.feature.room.mobile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzButton
import com.quizzgame.core.designsystem.mobile.QuizzButtonStyle
import com.quizzgame.feature.room.mobile.R
import com.quizzgame.feature.room.mobile.presentation.MobileLobbyIntent
import com.quizzgame.feature.room.mobile.presentation.MobileLobbyState
import com.quizzgame.feature.room.mobile.ui.tags.MobileLobbyTags
import com.quizzgame.shared.room.domain.entity.snapshot.RoomPhase
import com.quizzgame.shared.room.domain.entity.snapshot.RoomSnapshot
import com.quizzgame.shared.room.R as RoomR

@Composable
internal fun MobileLobbyReadyContent(
    state: MobileLobbyState,
    onCommand: (MobileLobbyIntent) -> Unit,
    onRetry: () -> Unit,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val room = requireNotNull(state.snapshot)
    val notice = state.notice()
    Column(modifier) {
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(top = 12.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "room_title") { MobileLobbyHero(room.entry.title, room.phase, state.gameInfo) }
            if (notice != null) {
                item(key = "room_notice") { MobileLobbyNotice(notice, onRetry) }
            }
            item(key = "roster_title") {
                val count = stringResource(RoomR.string.room_count, room.players.size, room.capacity.maxPlayers)
                MobileRosterHeading(count)
            }
            if (room.players.isEmpty()) {
                item(key = "roster_empty") { MobileRosterEmpty() }
            }
            items(room.players, key = { it.id.toString() }) { player ->
                MobileRoomPlayer(player, player.id == state.self)
            }
        }
        MobileLobbyActions(state, room, onCommand, onLeave)
    }
}

@Composable
private fun MobileLobbyActions(
    state: MobileLobbyState,
    room: RoomSnapshot,
    onCommand: (MobileLobbyIntent) -> Unit,
    onLeave: () -> Unit,
) {
    val ready = room.players.firstOrNull { it.id == state.self }?.ready == true
    val showReady = room.phase == RoomPhase.LOBBY && room.permissions.canReady
    Column(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp)) {
        if (showReady) {
            QuizzButton(
                { onCommand(MobileLobbyIntent.SetReady(!ready)) },
                Modifier.fillMaxWidth().testTag(MobileLobbyTags.READY),
                style = if (ready) QuizzButtonStyle.TONAL else QuizzButtonStyle.FILLED,
                enabled = state.live && !state.sending,
            ) {
                val name = if (ready) R.string.room_unset_ready else R.string.room_set_ready
                Text(stringResource(name))
            }
        }
        QuizzButton(
            onLeave,
            Modifier.fillMaxWidth().testTag(MobileLobbyTags.EXIT),
            style = QuizzButtonStyle.TEXT,
            enabled = !state.sending,
        ) { Text(stringResource(R.string.room_leave)) }
    }
}
