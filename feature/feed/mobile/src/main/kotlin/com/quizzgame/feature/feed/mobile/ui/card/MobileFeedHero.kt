package com.quizzgame.feature.feed.mobile.ui.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.media.QuizzMobileBadge
import com.quizzgame.core.designsystem.mobile.media.QuizzMobileMedia
import com.quizzgame.feature.feed.mobile.R
import com.quizzgame.feature.feed.mobile.ui.tags.MobileFeedTags
import com.quizzgame.shared.catalog.domain.entity.QuizCard

@Composable
internal fun MobileFeedHero(card: QuizCard, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Card(
        onClick,
        modifier.testTag(MobileFeedTags.card(card.id)),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow),
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.5f)),
    ) {
        QuizzMobileMedia(
            card.image,
            Modifier.fillMaxWidth().aspectRatio(HERO_ASPECT_RATIO).testTag(MobileFeedTags.artwork(card.id)),
            fadeTo = colors.surfaceContainerLow,
        )
        Column(
            Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                card.badge?.let {
                    QuizzMobileBadge(
                        it.text,
                        foreground = it.foreground,
                        background = it.background,
                        live = it.live,
                    )
                }
                QuizzMobileBadge(stringResource(R.string.feed_tv_label))
            }
            Text(
                card.title,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 3,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            card.subtitle?.let { Text(it, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant) }
        }
    }
}

private const val HERO_ASPECT_RATIO = 2f
