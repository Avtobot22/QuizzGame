package com.quizzgame.feature.room.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.designsystem.tv.QuizzTvButtonStyle
import com.quizzgame.core.ui.QuizzSceneIllustration
import com.quizzgame.feature.room.tv.R
import com.quizzgame.feature.room.tv.ui.tags.TvLobbyTags
import com.quizzgame.shared.room.domain.entity.snapshot.RoomConnection
import com.quizzgame.shared.room.domain.entity.snapshot.RoomParticipant
import com.quizzgame.shared.room.domain.entity.snapshot.RoomSnapshot
import com.quizzgame.shared.room.presentation.statusLabel
import com.quizzgame.core.ui.R as UiR
import com.quizzgame.shared.room.R as RoomR

internal val TvLobbyPlayerHeight = 68.dp
internal val TvLobbyPlayerColumns = GridCells.Adaptive(210.dp)

@Composable
internal fun TvRoomRoster(
    room: RoomSnapshot,
    canManage: Boolean,
    onManage: (RoomParticipant, FocusRequester) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth().testTag(TvLobbyTags.ROSTER)) {
        TvRosterHeading(stringResource(RoomR.string.room_count, room.players.size, room.capacity.maxPlayers))
        if (room.players.isEmpty()) {
            TvRosterEmpty(Modifier.weight(1f))
        } else {
            LazyVerticalGrid(
                columns = TvLobbyPlayerColumns,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(room.players, key = { it.id.toString() }) { player ->
                    TvRoomPlayer(player, canManage, onManage)
                }
            }
        }
    }
}

@Composable
internal fun TvRosterHeading(count: String?) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().heightIn(min = 38.dp).padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(RoomR.string.room_roster),
            Modifier.weight(1f).semantics { heading() },
            style = MaterialTheme.typography.titleLarge,
        )
        if (count != null) {
            Text(
                count,
                Modifier.background(colors.secondaryContainer, CircleShape)
                    .padding(horizontal = 14.dp, vertical = 5.dp),
                color = colors.onSecondaryContainer,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun TvRosterEmpty(modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier.fillMaxSize().background(colors.surface, MaterialTheme.shapes.extraLarge).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        QuizzSceneIllustration(colors.primary, colors.tertiary, Modifier.size(76.dp)) {
            Icon(painterResource(UiR.drawable.ic_quizz_profile), null, Modifier.size(30.dp), tint = colors.primary)
        }
        Text(stringResource(R.string.room_roster_empty), style = MaterialTheme.typography.titleLarge)
        Text(
            stringResource(R.string.room_roster_empty_description),
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun TvRoomPlayer(
    player: RoomParticipant,
    canManage: Boolean,
    onManage: (RoomParticipant, FocusRequester) -> Unit,
) {
    val focus = remember { FocusRequester() }
    val colors = MaterialTheme.colorScheme
    val modifier = Modifier.fillMaxWidth().heightIn(min = TvLobbyPlayerHeight)
        .testTag(TvLobbyTags.player(player.id.toString()))
        .background(colors.surfaceVariant.copy(alpha = 0.52f), MaterialTheme.shapes.medium)
    if (canManage) {
        val label = stringResource(R.string.room_manage_player)
        val select = { onManage(player, focus) }
        QuizzTvButton(
            select,
            modifier.focusRequester(focus).semantics {
                onClick(label = label) {
                    select()
                    true
                }
            },
            style = QuizzTvButtonStyle.QUIET,
        ) { TvPlayerIdentity(player) }
    } else {
        Box(modifier.padding(horizontal = 20.dp, vertical = 10.dp), contentAlignment = Alignment.CenterStart) {
            TvPlayerIdentity(player)
        }
    }
}

@Composable
private fun TvPlayerIdentity(player: RoomParticipant) {
    val colors = MaterialTheme.colorScheme
    val ready = player.ready && player.connection == RoomConnection.CONNECTED
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(36.dp).background(colors.secondaryContainer, CircleShape).clearAndSetSemantics {},
            contentAlignment = Alignment.Center,
        ) {
            val nameInitial = player.displayName.take(Character.charCount(player.displayName.codePointAt(0)))
            Text(nameInitial, color = colors.onSecondaryContainer, style = MaterialTheme.typography.titleSmall)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                player.displayName,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                stringResource(player.statusLabel()),
                color = if (ready) colors.primary else colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (ready) {
            Icon(painterResource(UiR.drawable.ic_quizz_check), null, Modifier.size(18.dp), tint = colors.primary)
        }
    }
}
