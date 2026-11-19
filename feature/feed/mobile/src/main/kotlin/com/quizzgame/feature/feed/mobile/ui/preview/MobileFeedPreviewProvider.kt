package com.quizzgame.feature.feed.mobile.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.paging.LoadState
import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.error.failure.ConnectivityKind
import com.quizzgame.shared.feed.domain.entity.Feed
import com.quizzgame.shared.feed.domain.entity.FeedPagingException

internal class MobileFeedPreviewProvider : PreviewParameterProvider<MobileFeedPreviewState> {
    private val compactFeed = Feed(
        listOf(
            MobileFeedPreviewFixtures.rail.copy(
                cards = MobileFeedPreviewFixtures.rail.cards.take(1),
            ),
        ),
    )
    private fun failure() = FeedPagingException(AppFailure.Connectivity(ConnectivityKind.OFFLINE_OR_UNREACHABLE))
    override val values = sequenceOf(
        MobileFeedPreviewState.Loading,
        MobileFeedPreviewState.Empty,
        MobileFeedPreviewState.Error(failure()),
        MobileFeedPreviewState.Content(MobileFeedPreviewFixtures.feed),
        MobileFeedPreviewState.Content(MobileFeedPreviewFixtures.feed, refresh = LoadState.Loading),
        MobileFeedPreviewState.Content(compactFeed, append = LoadState.Loading),
        MobileFeedPreviewState.Content(compactFeed, railAppend = LoadState.Loading),
        MobileFeedPreviewState.Content(MobileFeedPreviewFixtures.feed, refresh = LoadState.Error(failure())),
        MobileFeedPreviewState.Content(compactFeed, append = LoadState.Error(failure())),
        MobileFeedPreviewState.Content(compactFeed, railAppend = LoadState.Error(failure())),
    )
}
