package com.quizzgame.feature.feed.tv.ui

import android.animation.ValueAnimator
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.media.readableColor
import com.quizzgame.core.ui.tokens.QuizzTvLayout
import com.quizzgame.feature.feed.tv.R
import com.quizzgame.feature.feed.tv.domain.entity.QuizCard
import com.quizzgame.shared.catalog.domain.entity.PassingStatus
import com.quizzgame.core.ui.R as UiR

@Composable
internal fun TvQuizCard(card: QuizCard, onClick: () -> Unit, modifier: Modifier = Modifier, parallax: Boolean = false) {
    val interactions = remember { MutableInteractionSource() }
    val focused by interactions.collectIsFocusedAsState()
    val colors = MaterialTheme.colorScheme
    Card(
        onClick,
        modifier.width(QuizzTvLayout.cardWidth.dp).graphicsLayer {
            rotationY = if (parallax && focused && ValueAnimator.areAnimatorsEnabled()) -2f else 0f
        },
        interactionSource = interactions,
        shape = CardDefaults.shape(MaterialTheme.shapes.medium),
        colors = CardDefaults.colors(
            containerColor = colors.surface,
            contentColor = colors.onSurface,
            focusedContainerColor = colors.surface,
            focusedContentColor = colors.onSurface,
        ),
        scale = CardDefaults.scale(focusedScale = 1.04f),
        border = CardDefaults.border(
            border = Border(BorderStroke(1.dp, colors.border.copy(alpha = 0.16f)), shape = MaterialTheme.shapes.medium),
            focusedBorder = Border(BorderStroke(2.dp, colors.onSurface), 2.dp, MaterialTheme.shapes.medium),
        ),
        glow = CardDefaults.glow(),
    ) {
        TvQuizCardArtwork(card)
        TvQuizCardCopy(card)
    }
}

@Composable
private fun TvQuizCardCopy(card: QuizCard) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            card.title,
            color = card.titleColor.readableColor(colors.onSurface, colors.surface),
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            card.subtitle.orEmpty(),
            color = card.subtitleColor.readableColor(colors.onSurfaceVariant, colors.surface),
            maxLines = 1,
            minLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium,
        )
        TvQuizProgress(card)
    }
}

@Composable
private fun TvQuizProgress(card: QuizCard) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().height(30.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            if (card.user.bookmarked) Icon(painterResource(UiR.drawable.ic_quizz_bookmark), null, Modifier.size(14.dp))
            val label = when (card.user.status) {
                PassingStatus.COMPLETED -> stringResource(R.string.feed_quiz_completed)
                PassingStatus.IN_PROGRESS -> stringResource(
                    R.string.feed_quiz_progress,
                    (card.user.progress * 100).toInt(),
                )
                else -> if (card.user.bookmarked) stringResource(R.string.feed_quiz_bookmarked) else ""
            }
            Text(label, color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
        if (card.user.status == PassingStatus.IN_PROGRESS) {
            Box(Modifier.fillMaxWidth().height(3.dp).background(colors.surfaceVariant, MaterialTheme.shapes.small)) {
                Box(
                    Modifier.fillMaxWidth(card.user.progress.toFloat().coerceIn(0f, 1f)).height(3.dp)
                        .background(colors.primary, MaterialTheme.shapes.small),
                )
            }
        }
    }
}
