package com.quizzgame.mobile

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.text.AnnotatedString
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

internal class InputsScreen(private val provider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<InputsScreen>(provider) {
    val otp = KNode(provider) { hasContentDescription("Verification code") }
    val editable = KNode(provider) { hasSetTextAction() }

    fun roomCodeWithValue(value: String): KNode = KNode(provider) {
        addSemanticsMatcher(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString(value)))
    }

    fun text(value: String, useUnmergedTree: Boolean = false): KNode = KNode(provider) {
        this.useUnmergedTree = useUnmergedTree
        hasText(value)
    }

    fun assertEditableFieldCount(expected: Int) {
        if (expected > 0) editableAt(expected - 1).assertExists()
        editableAt(expected).assertDoesNotExist()
    }

    private fun editableAt(position: Int): KNode = KNode(provider) {
        hasSetTextAction()
        hasPosition(position)
    }
}
