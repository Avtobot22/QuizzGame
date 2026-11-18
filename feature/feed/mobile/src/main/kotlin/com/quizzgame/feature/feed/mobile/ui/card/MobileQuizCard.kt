package com.quizzgame.feature.feed.mobile.ui.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzMobileFavoriteMark
import com.quizzgame.core.designsystem.mobile.QuizzMobileLayout
import com.quizzgame.core.designsystem.mobile.QuizzMobileProgressTrack
import com.quizzgame.core.designsystem.mobile.media.QuizzMobileBadge
import com.quizzgame.core.designsystem.mobile.media.QuizzMobileMedia
import com.quizzgame.core.designsystem.mobile.media.readableColor
import com.quizzgame.core.ui.tokens.QuizzCatalogDimensions
import com.quizzgame.feature.feed.mobile.R
import com.quizzgame.feature.feed.mobile.ui.tags.MobileFeedTags
import com.quizzgame.shared.catalog.domain.entity.PassingStatus
import com.quizzgame.shared.catalog.domain.entity.QuizCard
import com.quizzgame.shared.catalog.domain.entity.QuizUserState

@Composable
internal fun MobileQuizCard(card: QuizCard, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Card(
        onClick,
        modifier.height(railCardHeight()).testTag(MobileFeedTags.card(card.id)),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow),
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        MobileQuizCardCover(card)
        MobileQuizCardCopy(card)
    }
}

@Composable
private fun MobileQuizCardCover(card: QuizCard) {
    Box(Modifier.fillMaxWidth().aspectRatio(QuizzMobileLayout.COVER_ASPECT_RATIO)) {
        QuizzMobileMedia(card.image, Modifier.matchParentSize().testTag(MobileFeedTags.artwork(card.id)))
        if (card.user.bookmarked) {
            QuizzMobileFavoriteMark(
                stringResource(R.string.feed_bookmarked),
                Modifier.align(Alignment.TopEnd).padding(10.dp),
            )
        }
        if (card.user.status != PassingStatus.NOT_STARTED) {
            MobileQuizCardStatus(card.user, Modifier.align(Alignment.BottomStart).padding(10.dp))
        }
    }
}

@Composable
private fun MobileQuizCardStatus(user: QuizUserState, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val completed = user.status == PassingStatus.COMPLETED
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Surface(
            shape = CircleShape,
            color = if (completed) colors.tertiaryContainer else colors.surfaceContainerLowest,
        ) {
            Text(
                stringResource(if (completed) R.string.feed_completed else R.string.feed_in_progress),
                Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelMedium,
                color = if (completed) colors.onTertiaryContainer else colors.primary,
                maxLines = 1,
            )
        }
        if (!completed) {
            QuizzMobileProgressTrack(user.progress.toFloat(), height = 5.dp)
        }
    }
}

@Composable
private fun MobileQuizCardCopy(card: QuizCard) {
    val colors = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxWidth().padding(horizontal = RailCopyPadding, vertical = RailCopyPadding),
        verticalArrangement = Arrangement.spacedBy(RailCopySpacing),
    ) {
        Box(Modifier.heightIn(min = railBadgeHeight())) {
            card.badge?.let {
                QuizzMobileBadge(it.text, foreground = it.foreground, background = it.background, live = it.live)
            }
        }
        Text(
            card.title,
            color = card.titleColor.readableColor(colors.onSurface, colors.surfaceContainerLow),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = TITLE_LINES,
            overflow = TextOverflow.Ellipsis,
        )
        card.subtitle?.let {
            Text(
                it,
                color = card.subtitleColor.readableColor(colors.onSurfaceVariant, colors.surfaceContainerLow),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun railCardHeight(): Dp = with(LocalDensity.current) {
    QuizzMobileLayout.railCardWidth / QuizzMobileLayout.COVER_ASPECT_RATIO + RailCopyPadding * 2 + RailCopySpacing * 2 +
        MaterialTheme.typography.titleMedium.lineHeight.toDp() * TITLE_LINES +
        MaterialTheme.typography.bodyMedium.lineHeight.toDp() + railBadgeHeight() + RailCopyRoundingAllowance
}

@Composable
internal fun railBadgeHeight(): Dp = with(LocalDensity.current) {
    MaterialTheme.typography.labelMedium.lineHeight.toDp() + QuizzCatalogDimensions.badgeVerticalPadding.dp * 2
}

internal val RailCopyPadding = 12.dp
internal val RailCopySpacing = 4.dp
private val RailCopyRoundingAllowance = 2.dp
private const val TITLE_LINES = 2
