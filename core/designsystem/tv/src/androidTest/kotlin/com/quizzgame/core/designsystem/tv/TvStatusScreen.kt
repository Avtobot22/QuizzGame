package com.quizzgame.core.designsystem.tv

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

internal class TvStatusScreen(private val provider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<TvStatusScreen>(provider) {
    val error = KNode(provider) { hasTestTag("error") }
    val host = KNode(provider) { hasText("Saved host content") }

    fun text(value: String): KNode = KNode(provider) { hasText(value) }

    fun action(label: String): KNode = KNode(provider) {
        hasText(label)
        hasClickAction()
    }
}
