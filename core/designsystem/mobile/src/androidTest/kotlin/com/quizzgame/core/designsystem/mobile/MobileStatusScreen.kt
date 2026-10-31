package com.quizzgame.core.designsystem.mobile

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

internal class MobileStatusScreen(private val provider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<MobileStatusScreen>(provider) {
    val error = KNode(provider) { hasTestTag("error") }
    val input = KNode(provider) { hasSetTextAction() }

    fun text(value: String): KNode = KNode(provider) { hasText(value) }

    fun action(label: String): KNode = KNode(provider) {
        hasText(label)
        hasClickAction()
    }
}
