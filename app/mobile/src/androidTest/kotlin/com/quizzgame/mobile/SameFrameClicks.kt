package com.quizzgame.mobile

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText

/** Kakao не предоставляет два нажатия одного узла до следующего кадра. */
internal fun clickTwiceBeforeNextFrame(compose: ComposeTestRule, text: String) {
    val click = checkNotNull(compose.onNodeWithText(text).fetchSemanticsNode().config[SemanticsActions.OnClick].action)
    compose.runOnIdle {
        click()
        click()
    }
}
