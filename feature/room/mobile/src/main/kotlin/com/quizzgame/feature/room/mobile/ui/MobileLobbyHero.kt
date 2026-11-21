package com.quizzgame.feature.room.mobile.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quizzgame.core.ui.QuizzIconLabel
import com.quizzgame.feature.room.mobile.R
import com.quizzgame.feature.room.mobile.presentation.MobileLobbyGameInfo
import com.quizzgame.shared.room.domain.entity.snapshot.RoomPhase
import com.quizzgame.core.ui.R as UiR
import com.quizzgame.shared.room.R as RoomR

internal val MobileLobbyHeroHeight = 168.dp
private const val SECONDS_PER_MINUTE = 60

@Composable
internal fun MobileLobbyHero(title: String, phase: RoomPhase, gameInfo: MobileLobbyGameInfo?) {
    val colors = MaterialTheme.colorScheme
    val tint = lerp(colors.surfaceContainer, colors.primaryContainer, 0.46f)
    Column(
        Modifier.fillMaxWidth().heightIn(min = MobileLobbyHeroHeight)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(Brush.linearGradient(listOf(tint, colors.surfaceContainerLow)))
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                title,
                Modifier.semantics { heading() },
                style = MaterialTheme.typography.headlineSmall,
                color = colors.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            gameInfo?.description?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (gameInfo != null) MobileLobbyGameMetadata(gameInfo)
        HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.65f))
        MobileLobbyGameStatus(phase)
    }
}

@Composable
private fun MobileLobbyGameMetadata(info: MobileLobbyGameInfo) {
    if (info.questionCount == null && info.durationSeconds == null) return
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        info.questionCount?.let { count ->
            MobileGameFact(
                UiR.drawable.ic_quizz_quiz,
                pluralStringResource(R.plurals.room_game_questions, count, count),
            )
        }
        info.durationSeconds?.let { seconds ->
            val minutes = (seconds - 1) / SECONDS_PER_MINUTE + 1
            MobileGameFact(
                UiR.drawable.ic_quizz_clock,
                pluralStringResource(R.plurals.room_game_minutes, minutes, minutes),
            )
        }
    }
}

@Composable
private fun MobileGameFact(@DrawableRes icon: Int, label: String) {
    val colors = MaterialTheme.colorScheme
    Surface(
        color = colors.surfaceContainerLowest.copy(alpha = 0.8f),
        contentColor = colors.onSurfaceVariant,
        shape = MaterialTheme.shapes.small,
    ) {
        QuizzIconLabel(
            icon,
            Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
            iconSize = 15.dp,
            tint = colors.primary,
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun MobileLobbyGameStatus(phase: RoomPhase) {
    val colors = MaterialTheme.colorScheme
    val label = if (phase == RoomPhase.STARTING) RoomR.string.room_starting else R.string.room_wait_host
    Row(
        Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(7.dp).background(colors.primary, CircleShape))
        Text(stringResource(label), style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
    }
}
