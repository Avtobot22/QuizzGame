package com.quizzgame.feature.feed.tv.ui.rail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvSkeleton
import com.quizzgame.core.designsystem.tv.QuizzTvSkeletonCard
import com.quizzgame.core.ui.tokens.QuizzTvLayout
import com.quizzgame.feature.feed.tv.R
import com.quizzgame.feature.feed.tv.domain.entity.QuizRail
import com.quizzgame.feature.feed.tv.ui.card.TvQuizCard
import com.quizzgame.feature.feed.tv.ui.tags.TvFeedTags

@Composable
internal fun TvQuizRail(
    rail: QuizRail,
    onOpen: (String) -> Unit,
    parallax: Boolean,
    appending: Boolean = false,
    entryFocus: FocusRequester? = null,
    headerAction: (@Composable () -> Unit)? = null,
) {
    val focus = remember { FocusRequester() }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        TvQuizRailHeader(rail, headerAction)
        LazyRow(
            modifier = Modifier.focusRequester(focus)
                .then(if (entryFocus != null) Modifier.focusRequester(entryFocus) else Modifier).focusRestorer(),
            contentPadding = PaddingValues(
                horizontal = QuizzTvLayout.screenInset.dp,
                vertical = QuizzTvLayout.focusInset.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(QuizzTvLayout.itemGap.dp),
        ) {
            items(rail.cards, key = { it.id }) { card ->
                TvQuizCard(
                    card,
                    onClick = {
                        focus.saveFocusedChild()
                        onOpen(card.entryId)
                    },
                    modifier = Modifier.testTag(TvFeedTags.card(card.id)),
                    parallax = parallax,
                )
            }
            if (appending) {
                item(key = "cards-loading") {
                    QuizzTvSkeleton(
                        stringResource(R.string.feed_loading_cards),
                        Modifier.testTag(TvFeedTags.appendingRail(rail.id)),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(QuizzTvLayout.itemGap.dp)) {
                            repeat(2) { QuizzTvSkeletonCard() }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TvQuizRailHeader(rail: QuizRail, action: (@Composable () -> Unit)?) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = QuizzTvLayout.screenInset.dp)
            .heightIn(min = QuizzTvLayout.railHeaderHeight.dp),
        horizontalArrangement = Arrangement.spacedBy(QuizzTvLayout.itemGap.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            rail.title?.let {
                Text(it, Modifier.semantics { heading() }, style = MaterialTheme.typography.titleLarge)
            }
            rail.subtitle?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        action?.invoke()
    }
}
