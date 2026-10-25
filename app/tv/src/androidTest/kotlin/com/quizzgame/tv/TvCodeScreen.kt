package com.quizzgame.tv

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import com.quizzgame.core.ui.R
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

internal class TvCodeScreen(
    provider: SemanticsNodeInteractionsProvider,
    label: String = "Room code",
) : ComposeScreen<TvCodeScreen>(provider) {
    private val components = TvComponentScreen(provider)

    val input = KNode(provider) { hasContentDescription(label) }
    val delete = KNode(provider) { hasContentDescription(R.string.quizz_tv_code_delete) }
    val resend = components.text("Resend code")

    fun character(value: String): KNode = components.text(value)

    fun assertFocusableCount(expected: Int) = components.assertFocusableCount(expected)

    fun assertUnmergedTextCount(value: String, expected: Int) = components.assertTextCount(value, expected, true)
}
