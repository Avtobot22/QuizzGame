package com.quizzgame.feature.feed.mobile.ui.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.quizzgame.core.designsystem.mobile.QuizzMobileLayout
import com.quizzgame.feature.feed.mobile.R
import com.quizzgame.feature.feed.mobile.presentation.MobileFeedIntent
import com.quizzgame.feature.feed.mobile.ui.card.MobileFeedHero
import com.quizzgame.feature.feed.mobile.ui.rail.MobileQuizRail
import com.quizzgame.feature.feed.mobile.ui.status.MobileFeedAppendSkeleton
import com.quizzgame.feature.feed.mobile.ui.status.MobileFeedPageError
import com.quizzgame.feature.feed.mobile.ui.tags.MobileFeedTags
import com.quizzgame.shared.catalog.domain.entity.QuizCard
import com.quizzgame.shared.feed.domain.entity.section.FeedSection
import com.quizzgame.shared.feed.domain.entity.section.QuizHero
import com.quizzgame.shared.feed.domain.entity.section.QuizRail
import kotlinx.coroutines.flow.Flow

@Composable
internal fun MobileFeedSections(
    sections: LazyPagingItems<FeedSection>,
    items: (QuizRail) -> Flow<PagingData<QuizCard>>,
    onIntent: (MobileFeedIntent) -> Unit,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val refresh = sections.loadState.refresh
    LazyColumn(
        modifier.fillMaxSize().testTag(MobileFeedTags.LIST),
        contentPadding = PaddingValues(vertical = 12.dp),
    ) {
        if (refresh is LoadState.Error) {
            item {
                MobileFeedPageError(
                    refresh.error,
                    true,
                    sections::retry,
                    onSignIn,
                    tag = MobileFeedTags.REFRESH_ERROR,
                )
            }
        }
        items(sections.itemCount, key = sections.itemKey { it.id }) { index ->
            when (val section = sections[index]) {
                is QuizHero -> FeaturedSection(section.card) { onIntent(MobileFeedIntent.Open(section.card.entryId)) }
                is QuizRail -> MobileQuizRail(
                    section,
                    remember(section) { items(section) },
                    { onIntent(MobileFeedIntent.Open(it)) },
                    onSignIn,
                )
                null -> Unit
            }
        }
        val append = sections.loadState.append
        if (append is LoadState.Loading) {
            item {
                MobileFeedAppendSkeleton(Modifier.testTag(MobileFeedTags.APPENDING_SECTIONS))
            }
        }
        if (append is LoadState.Error) {
            item {
                MobileFeedPageError(
                    append.error,
                    true,
                    sections::retry,
                    onSignIn,
                    tag = MobileFeedTags.APPEND_ERROR,
                )
            }
        }
    }
}

@Composable
private fun FeaturedSection(card: QuizCard, onOpen: () -> Unit) {
    val gutter = QuizzMobileLayout.pageGutter
    Column(
        Modifier.fillMaxWidth().padding(start = gutter, end = gutter, bottom = gutter),
        verticalArrangement = Arrangement.spacedBy(gutter),
    ) {
        MobileFeedSectionHeader(
            stringResource(R.string.feed_featured),
            Modifier.testTag(MobileFeedTags.FEATURED_HEADING),
        )
        MobileFeedHero(card, onOpen, Modifier.fillMaxWidth())
    }
}
