package com.quizzgame.tv

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

internal class AppShellScreen(private val provider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<AppShellScreen>(provider) {
    fun tag(value: String): KNode = KNode(provider) { hasTestTag(value) }
    fun text(value: String): KNode = KNode(provider) { hasText(value) }
}
