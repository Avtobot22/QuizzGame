package com.quizzgame.feature.feed.tv.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.tv.media.LocalTvMediaLoader
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.ui.tv.components.QuizzTvErrorBanner
import com.quizzgame.core.media.MediaSpec
import com.quizzgame.core.navigation.focus.TvEntryFocus
import com.quizzgame.core.ui.tokens.QuizzTvLayout
import com.quizzgame.feature.feed.tv.presentation.TvFeedIntent
import com.quizzgame.feature.feed.tv.ui.paging.TvFeedRefreshAction
import com.quizzgame.feature.feed.tv.ui.rail.TvQuizRail
import com.quizzgame.feature.feed.tv.ui.status.TvFeedAppendingSections
import com.quizzgame.feature.feed.tv.ui.tags.TvFeedTags
import com.quizzgame.shared.feed.domain.entity.section.QuizRail

@Composable
internal fun TvFeedPreviewContent(
    state: TvFeedPreviewState.Content,
    onIntent: (TvFeedIntent) -> Unit,
    requestFocus: Boolean,
    modifier: Modifier,
    onSignIn: () -> Unit,
) {
    val focus = remember { FocusRequester() }
    val firstRailFocus = remember { FocusRequester() }
    val images = if (state.prefetch.enabled) LocalTvMediaLoader.current else null
    LaunchedEffect(state.value, state.prefetch) {
        if (images != null) {
            val candidates = state.value.sections.filterIsInstance<QuizRail>().take(
                state.prefetch.depth,
            ).flatMap { rail ->
                rail.cards.take(state.prefetch.depth).mapNotNull { (it.image as? MediaSpec.Remote)?.url }
            }
            images.prefetch(candidates, state.prefetch.maxConcurrent)
        }
    }
    Column(modifier.fillMaxSize().testTag(TvFeedTags.CONTENT)) {
        LazyColumn(
            modifier = Modifier.weight(1f).focusRequester(focus).focusRestorer(
                fallback = if (state.error == null) firstRailFocus else FocusRequester.Default,
            ),
            contentPadding = PaddingValues(top = 8.dp, bottom = QuizzTvLayout.verticalInset.dp),
            verticalArrangement = Arrangement.spacedBy(QuizzTvLayout.sectionGap.dp),
        ) {
            state.error?.let { spec ->
                item(key = "refresh-error") {
                    QuizzTvErrorBanner(
                        spec,
                        { action ->
                            when (action) {
                                ErrorActionKind.RETRY -> onIntent(TvFeedIntent.Retry)
                                ErrorActionKind.DISMISS -> Unit
                                ErrorActionKind.SIGN_IN -> onSignIn()
                                else -> Unit
                            }
                        },
                        Modifier.padding(horizontal = QuizzTvLayout.screenInset.dp),
                    )
                }
            }
            feedRails(state, onIntent, focus, firstRailFocus)
            if (state.appendingSections) {
                item(key = "sections-loading") {
                    TvFeedAppendingSections()
                }
            }
        }
    }
    TvEntryFocus(focus, requestFocus)
}

private fun androidx.compose.foundation.lazy.LazyListScope.feedRails(
    state: TvFeedPreviewState.Content,
    onIntent: (TvFeedIntent) -> Unit,
    focus: FocusRequester,
    firstRailFocus: FocusRequester,
) {
    itemsIndexed(state.value.sections.filterIsInstance<QuizRail>(), key = { _, rail -> rail.id }) { index, rail ->
        TvQuizRail(
            rail,
            onOpen = {
                focus.saveFocusedChild()
                onIntent(TvFeedIntent.Open(it))
            },
            parallax = state.parallax,
            appending = rail.id in state.appendingRails,
            entryFocus = firstRailFocus.takeIf { index == 0 },
            headerAction = if (index == 0) {
                { TvFeedRefreshAction(state.refreshing) { onIntent(TvFeedIntent.Refresh) } }
            } else {
                null
            },
        )
    }
}
