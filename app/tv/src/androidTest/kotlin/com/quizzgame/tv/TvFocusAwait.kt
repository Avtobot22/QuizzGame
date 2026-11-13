package com.quizzgame.tv

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag

/** Видимый во время enter-анимации экран ещё не обязан владеть фокусом. */
internal fun ComposeTestRule.awaitFocused(tag: String) {
    waitUntil(5_000) {
        onNodeWithTag(tag).fetchSemanticsNode().config.getOrElse(SemanticsProperties.Focused) { false }
    }
}
