package com.quizzgame.mobile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.quizzgame.core.ui.R
import org.junit.Rule
import org.junit.Test

class WelcomeScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun instructionsRemainVisibleAfterActivityRecreation() {
        val show = compose.activity.getString(R.string.quizz_show_instructions)
        val instructions = compose.activity.getString(R.string.quizz_instructions)

        compose.onNodeWithText(show).performClick()
        compose.onNodeWithText(instructions).assertIsDisplayed()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText(instructions).assertIsDisplayed()
        compose.onNodeWithText(compose.activity.getString(R.string.quizz_hide_instructions)).performClick()
        compose.onNodeWithText(instructions).assertDoesNotExist()
    }
}
