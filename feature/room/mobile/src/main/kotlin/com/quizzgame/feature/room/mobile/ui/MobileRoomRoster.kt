package com.quizzgame.feature.room.mobile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quizzgame.feature.room.mobile.R
import com.quizzgame.feature.room.mobile.ui.tags.MobileLobbyTags
import com.quizzgame.shared.room.domain.entity.snapshot.RoomConnection
import com.quizzgame.shared.room.domain.entity.snapshot.RoomParticipant
import com.quizzgame.shared.room.presentation.statusLabel
import com.quizzgame.shared.room.R as RoomR

internal val MobileLobbyPlayerHeight = 64.dp

@Composable
internal fun MobileRosterHeading(count: String?) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().testTag(MobileLobbyTags.ROSTER).padding(top = 12.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(RoomR.string.room_roster),
            Modifier.weight(1f).semantics { heading() },
            style = MaterialTheme.typography.titleMedium,
        )
        if (count != null) {
            Text(count, style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
        }
    }
}

@Composable
internal fun MobileRoomPlayer(player: RoomParticipant, self: Boolean) {
    val colors = MaterialTheme.colorScheme
    val ready = player.ready && player.connection == RoomConnection.CONNECTED
    val name = if (self) stringResource(R.string.room_self, player.displayName) else player.displayName
    Surface(
        Modifier.fillMaxWidth().testTag(MobileLobbyTags.player(player.id.toString())),
        shape = MaterialTheme.shapes.large,
        color = if (self) colors.surfaceContainerHigh else colors.surfaceContainerLow,
    ) {
        Row(
            Modifier.heightIn(min = MobileLobbyPlayerHeight).padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = colors.secondaryContainer,
                contentColor = colors.onSecondaryContainer,
            ) {
                Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                    val nameInitial = player.displayName.take(Character.charCount(player.displayName.codePointAt(0)))
                    Text(
                        nameInitial,
                        Modifier.clearAndSetSemantics {},
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
            Text(
                name,
                Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Surface(
                shape = CircleShape,
                color = if (ready) colors.primaryContainer else colors.surfaceContainerHighest,
                contentColor = if (ready) colors.onPrimaryContainer else colors.onSurfaceVariant,
            ) {
                Text(
                    stringResource(player.statusLabel()),
                    Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
internal fun MobileRosterEmpty() {
    val colors = MaterialTheme.colorScheme
    Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = colors.surfaceContainerLow) {
        Text(
            stringResource(RoomR.string.room_keep_waiting),
            Modifier.padding(18.dp),
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
