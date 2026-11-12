package com.quizzgame.core.navigation.chrome

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

internal class ChromeScreen(private val provider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<ChromeScreen>(provider) {
    fun tag(value: String): KNode = KNode(provider) { hasTestTag(value) }
    fun assertOneChrome() { provider.onAllNodesWithTag(NavigationChromeTestTags.CHROME).assertCountEquals(1) }
}
