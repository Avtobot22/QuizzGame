package com.quizzgame.tv

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import com.quizzgame.core.ui.R
import org.junit.Rule
import org.junit.Test

class RemoteControlTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun remoteCenterKeyOpensAndClosesInstructions() {
        val show = compose.activity.getString(R.string.quizz_show_instructions)
        val hide = compose.activity.getString(R.string.quizz_hide_instructions)
        val instructions = compose.activity.getString(R.string.quizz_instructions)

        compose.onNodeWithText(show).assertIsFocused().performKeyInput { pressKey(Key.DirectionCenter) }
        compose.onNodeWithText(instructions).assertIsDisplayed()
        compose.onNodeWithText(hide).assertIsFocused().performKeyInput { pressKey(Key.DirectionCenter) }
        compose.onNodeWithText(instructions).assertDoesNotExist()
    }
}
