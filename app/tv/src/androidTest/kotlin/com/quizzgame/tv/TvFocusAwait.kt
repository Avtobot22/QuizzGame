package com.quizzgame.tv

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag

/** Видимый во время enter-анимации экран ещё не обязан владеть фокусом. */
internal fun ComposeTestRule.awaitFocused(tag: String) {
    waitUntil(5_000) {
        onAllNodesWithTag(tag).fetchSemanticsNodes().any {
            it.config.getOrElse(SemanticsProperties.Focused) { false }
        }
    }
}
