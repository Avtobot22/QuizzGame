package com.quizzgame.feature.feed.mobile.ui.content

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import com.quizzgame.feature.feed.mobile.presentation.MobileFeedIntent
import com.quizzgame.feature.feed.mobile.ui.status.MobileFeedEmpty
import com.quizzgame.feature.feed.mobile.ui.status.MobileFeedPageError
import com.quizzgame.feature.feed.mobile.ui.status.MobileFeedSkeleton
import com.quizzgame.feature.feed.mobile.ui.tags.MobileFeedTags
import com.quizzgame.shared.catalog.domain.entity.QuizCard
import com.quizzgame.shared.feed.domain.entity.section.FeedSection
import com.quizzgame.shared.feed.domain.entity.section.QuizRail
import kotlinx.coroutines.flow.Flow

@Composable
internal fun MobileFeedContent(
    sections: LazyPagingItems<FeedSection>,
    items: (QuizRail) -> Flow<PagingData<QuizCard>>,
    onIntent: (MobileFeedIntent) -> Unit,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val refresh = sections.loadState.refresh
    when {
        sections.itemCount > 0 -> MobileFeedSections(sections, items, onIntent, onSignIn, modifier)
        refresh is LoadState.Loading -> MobileFeedSkeleton(modifier.testTag(MobileFeedTags.LOADING))
        refresh is LoadState.Error -> MobileFeedPageError(refresh.error, false, sections::retry, onSignIn, modifier)
        else -> MobileFeedEmpty({ onIntent(MobileFeedIntent.Refresh) }, modifier.testTag(MobileFeedTags.EMPTY))
    }
}
