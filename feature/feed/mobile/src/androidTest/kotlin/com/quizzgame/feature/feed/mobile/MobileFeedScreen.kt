package com.quizzgame.feature.feed.mobile

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.DpSize
import com.quizzgame.feature.feed.mobile.ui.tags.MobileFeedTags
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

internal class MobileFeedScreen(private val provider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<MobileFeedScreen>(provider) {
    fun bounds(tag: String): Rect = provider.onNodeWithTag(tag, useUnmergedTree = true)
        .fetchSemanticsNode().boundsInRoot
    fun unclippedSize(tag: String): DpSize {
        val bounds = provider.onNodeWithTag(tag, useUnmergedTree = true).getUnclippedBoundsInRoot()
        return DpSize(bounds.right - bounds.left, bounds.bottom - bounds.top)
    }
    fun cardTextBounds(id: String, text: String): Rect = provider.onNode(
        hasText(text) and hasAnyAncestor(hasTestTag(MobileFeedTags.card(id))),
        useUnmergedTree = true,
    ).fetchSemanticsNode().boundsInRoot
    fun card(id: String) = tag(MobileFeedTags.card(id))
    fun tag(value: String) = KNode(provider) { hasTestTag(value) }
    fun text(value: String) = KNode(provider) { hasText(value) }
}
