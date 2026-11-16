package com.quizzgame.feature.feed.tv

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import com.quizzgame.feature.feed.tv.ui.tags.TvFeedTags
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

internal class FeedPagingScreen(private val provider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<FeedPagingScreen>(provider) {
    fun retry(sectionId: String) = KNode(provider) { hasTestTag(TvFeedTags.retryRail(sectionId)) }
    fun card(id: String) = KNode(provider) { hasTestTag(TvFeedTags.card(id)) }
}
