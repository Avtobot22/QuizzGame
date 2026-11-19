package com.quizzgame.feature.feed.mobile.ui.rail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.quizzgame.core.designsystem.mobile.QuizzMobileLayout
import com.quizzgame.feature.feed.mobile.ui.card.MobileQuizCard
import com.quizzgame.feature.feed.mobile.ui.card.railCardHeight
import com.quizzgame.feature.feed.mobile.ui.content.MobileFeedSectionHeader
import com.quizzgame.feature.feed.mobile.ui.status.MobileFeedPageError
import com.quizzgame.feature.feed.mobile.ui.status.MobileRailSkeleton
import com.quizzgame.feature.feed.mobile.ui.tags.MobileFeedTags
import com.quizzgame.shared.catalog.domain.entity.QuizCard
import com.quizzgame.shared.feed.domain.entity.section.QuizRail
import kotlinx.coroutines.flow.Flow

@Composable
internal fun MobileQuizRail(
    rail: QuizRail,
    pages: Flow<PagingData<QuizCard>>,
    onOpen: (String) -> Unit,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cards = pages.collectAsLazyPagingItems()
    Column(modifier.testTag(MobileFeedTags.section(rail.id))) {
        MobileFeedSectionHeader(
            rail.title,
            Modifier.padding(horizontal = QuizzMobileLayout.pageGutter),
            subtitle = rail.subtitle,
        )
        LazyRow(
            modifier = Modifier.testTag(MobileFeedTags.row(rail.id)),
            contentPadding = PaddingValues(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(cards.itemCount, key = cards.itemKey { it.id }) { index ->
                cards[index]?.let { card ->
                    MobileQuizCard(card, { onOpen(card.entryId) }, Modifier.width(QuizzMobileLayout.railCardWidth))
                }
            }
            if (cards.itemCount == 0 && cards.loadState.refresh is LoadState.Loading) {
                item { MobileRailSkeleton(Modifier.width(QuizzMobileLayout.railCardWidth)) }
            }
            val append = cards.loadState.append
            if (append is LoadState.Loading) {
                item {
                    MobileRailSkeleton(
                        Modifier.width(QuizzMobileLayout.railCardWidth).testTag(MobileFeedTags.appendingRail(rail.id)),
                    )
                }
            }
            if (append is LoadState.Error) {
                item {
                    MobileFeedPageError(
                        append.error,
                        true,
                        cards::retry,
                        onSignIn,
                        Modifier.width(QuizzMobileLayout.railCardWidth).height(railCardHeight()),
                        tag = MobileFeedTags.retryRail(rail.id),
                        compact = true,
                    )
                }
            }
        }
        val refresh = cards.loadState.refresh
        if (refresh is LoadState.Error) {
            MobileFeedPageError(
                refresh.error,
                true,
                cards::retry,
                onSignIn,
                tag = MobileFeedTags.retryRail(rail.id),
            )
        }
    }
}
