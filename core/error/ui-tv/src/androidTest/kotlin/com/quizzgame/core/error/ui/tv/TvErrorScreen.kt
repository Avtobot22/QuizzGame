package com.quizzgame.core.error.ui.tv

import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.onNodeWithTag
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode
import org.junit.Assert.assertEquals

internal class TvErrorScreen(private val provider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<TvErrorScreen>(provider) {
    val error = KNode(provider) { hasTestTag("error") }
    val host = KNode(provider) { hasText("Saved host content") }

    fun text(value: String): KNode = KNode(provider) { hasText(value) }

    fun action(label: String): KNode = KNode(provider) {
        hasText(label)
        hasClickAction()
    }

    fun assertPanelHorizontallyCentered() {
        val parent = provider.onNodeWithTag("error").fetchSemanticsNode().boundsInRoot
        val body = provider.onNode(
            SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite),
            useUnmergedTree = true,
        ).fetchSemanticsNode().boundsInRoot
        assertEquals("Status panel must be centered within its owner", parent.center.x, body.center.x, 1f)
    }
}
