package com.quizzgame.mobile

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

internal class MobileCatalogScreen(private val provider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<MobileCatalogScreen>(provider) {
    fun tag(value: String): KNode = KNode(provider) { hasTestTag(value) }
}
