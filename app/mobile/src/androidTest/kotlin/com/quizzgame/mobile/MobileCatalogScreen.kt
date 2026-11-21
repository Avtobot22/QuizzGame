package com.quizzgame.mobile

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

internal class MobileCatalogScreen(private val provider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<MobileCatalogScreen>(provider) {
    fun tag(value: String, useUnmergedTree: Boolean = false): KNode = KNode(provider) {
        this.useUnmergedTree = useUnmergedTree
        hasTestTag(value)
    }

    fun text(value: String): KNode = KNode(provider) { hasText(value) }
}
