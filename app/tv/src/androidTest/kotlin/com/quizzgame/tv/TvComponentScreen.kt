package com.quizzgame.tv

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.hasText
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

internal class TvComponentScreen(private val provider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<TvComponentScreen>(provider) {
    fun answer(title: String): KNode = text(title)

    fun text(value: String, useUnmergedTree: Boolean = false): KNode = KNode(provider) {
        this.useUnmergedTree = useUnmergedTree
        hasText(value)
    }

    fun assertFocusableCount(expected: Int) {
        assertNodeCount(SemanticsMatcher.keyIsDefined(SemanticsProperties.Focused), expected)
    }

    fun assertTextCount(value: String, expected: Int, useUnmergedTree: Boolean = false) {
        assertNodeCount(hasText(value), expected, useUnmergedTree)
    }

    private fun assertNodeCount(matcher: SemanticsMatcher, expected: Int, useUnmergedTree: Boolean = false) {
        if (expected > 0) nodeAt(matcher, expected - 1, useUnmergedTree).assertExists()
        nodeAt(matcher, expected, useUnmergedTree).assertDoesNotExist()
    }

    private fun nodeAt(matcher: SemanticsMatcher, position: Int, useUnmergedTree: Boolean): KNode = KNode(provider) {
        this.useUnmergedTree = useUnmergedTree
        addSemanticsMatcher(matcher)
        hasPosition(position)
    }
}
