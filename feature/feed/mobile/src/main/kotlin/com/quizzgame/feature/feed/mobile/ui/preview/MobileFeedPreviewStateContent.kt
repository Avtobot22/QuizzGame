package com.quizzgame.feature.feed.mobile.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.quizzgame.feature.feed.mobile.presentation.MobileFeedIntent
import com.quizzgame.feature.feed.mobile.ui.MobileFeedStateContent
import com.quizzgame.shared.feed.domain.entity.section.FeedSection
import com.quizzgame.shared.feed.domain.entity.section.QuizRail
import kotlinx.coroutines.flow.flowOf

@Composable
internal fun MobileFeedPreviewStateContent(
    state: MobileFeedPreviewState,
    modifier: Modifier = Modifier,
    onIntent: (MobileFeedIntent) -> Unit = {},
) {
    val content = state as? MobileFeedPreviewState.Content
    val refresh = when (state) {
        MobileFeedPreviewState.Loading -> LoadState.Loading
        is MobileFeedPreviewState.Error -> LoadState.Error(state.cause)
        else -> content?.refresh ?: LoadState.NotLoading(true)
    }
    val pages = remember(state) {
        flowOf(
            PagingData.from<FeedSection>(
                content?.value?.sections.orEmpty(),
                sourceLoadStates = previewLoadStates(refresh, content?.append ?: LoadState.NotLoading(true)),
            ),
        )
    }
    val rails = remember(state) {
        content?.value?.sections.orEmpty().filterIsInstance<QuizRail>()
            .associate { rail ->
                rail.id to flowOf(
                    PagingData.from(
                        rail.cards,
                        sourceLoadStates = previewLoadStates(
                            LoadState.NotLoading(false), content?.railAppend ?: LoadState.NotLoading(true),
                        ),
                    ),
                )
            }
    }
    val sections = pages.collectAsLazyPagingItems()
    MobileFeedStateContent(
        sections,
        items = { rail -> checkNotNull(rails[rail.id]) },
        onIntent = onIntent,
        onSignIn = {},
        modifier = modifier,
    )
}

private fun previewLoadStates(refresh: LoadState, append: LoadState) = LoadStates(
    refresh = refresh,
    prepend = LoadState.NotLoading(true),
    append = append,
)
