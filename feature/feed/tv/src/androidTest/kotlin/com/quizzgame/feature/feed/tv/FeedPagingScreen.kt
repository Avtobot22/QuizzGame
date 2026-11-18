package com.quizzgame.feature.feed.tv

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import com.quizzgame.feature.feed.tv.ui.tags.TvFeedTags
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

internal class FeedPagingScreen(private val provider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<FeedPagingScreen>(provider) {
    fun retry(sectionId: String) = KNode(provider) { hasTestTag(TvFeedTags.retryRail(sectionId)) }
    fun card(id: String) = KNode(provider) { hasTestTag(TvFeedTags.card(id)) }
    fun artworkBounds(id: String): Rect = provider.onNodeWithTag(TvFeedTags.artwork(id), useUnmergedTree = true)
        .fetchSemanticsNode().boundsInRoot
    fun cardTextBounds(id: String, text: String): Rect = provider.onNode(
        hasText(text) and hasAnyAncestor(hasTestTag(TvFeedTags.card(id))),
        useUnmergedTree = true,
    ).fetchSemanticsNode().boundsInRoot
}
