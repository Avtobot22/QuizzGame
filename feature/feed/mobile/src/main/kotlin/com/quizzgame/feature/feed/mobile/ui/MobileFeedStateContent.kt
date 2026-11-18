package com.quizzgame.feature.feed.mobile.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import com.quizzgame.core.designsystem.mobile.QuizzScreenScaffold
import com.quizzgame.core.designsystem.mobile.QuizzTopBar
import com.quizzgame.feature.feed.mobile.R
import com.quizzgame.feature.feed.mobile.presentation.MobileFeedIntent
import com.quizzgame.feature.feed.mobile.ui.content.MobileFeedContent
import com.quizzgame.feature.feed.mobile.ui.status.MobileFeedRefreshIndicator
import com.quizzgame.feature.feed.mobile.ui.tags.MobileFeedTags
import com.quizzgame.shared.catalog.domain.entity.QuizCard
import com.quizzgame.shared.feed.domain.entity.FeedSection
import com.quizzgame.shared.feed.domain.entity.QuizRail
import kotlinx.coroutines.flow.Flow

@Composable
internal fun MobileFeedStateContent(
    sections: LazyPagingItems<FeedSection>,
    items: (QuizRail) -> Flow<PagingData<QuizCard>>,
    onIntent: (MobileFeedIntent) -> Unit,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val refreshing = sections.loadState.refresh is LoadState.Loading
    val pullState = rememberPullToRefreshState()
    val contentRefreshing = refreshing && sections.itemCount > 0
    val refreshLabel = stringResource(R.string.feed_refresh)
    val refresh = { onIntent(MobileFeedIntent.Refresh) }
    QuizzScreenScaffold(modifier.testTag(MobileFeedTags.ROOT), topBar = {
        QuizzTopBar(stringResource(R.string.feed_title))
    }) { padding ->
        PullToRefreshBox(
            isRefreshing = contentRefreshing,
            state = pullState,
            indicator = {
                if (contentRefreshing || pullState.distanceFraction > 0f) {
                    MobileFeedRefreshIndicator(
                        contentRefreshing,
                        pullState.distanceFraction,
                        Modifier.align(Alignment.TopCenter).padding(top = 4.dp),
                    )
                }
            },
            onRefresh = refresh,
            modifier = Modifier.padding(padding).fillMaxSize().clipToBounds()
                .testTag(MobileFeedTags.REFRESH).semantics {
                    customActions = listOf(
                        CustomAccessibilityAction(refreshLabel) {
                            if (!refreshing) refresh()
                            true
                        },
                    )
                },
        ) {
            MobileFeedContent(
                sections,
                items,
                onIntent,
                onSignIn,
                Modifier.offset(y = 72.dp * pullState.distanceFraction.coerceIn(0f, 1f)),
            )
        }
    }
}
