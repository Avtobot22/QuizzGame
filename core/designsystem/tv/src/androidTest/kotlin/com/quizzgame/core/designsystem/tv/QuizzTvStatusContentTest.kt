package com.quizzgame.core.designsystem.tv

import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.tv.material3.Text
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class QuizzTvStatusContentTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val screen: TvStatusScreen
        get() = TvStatusScreen(compose)

    @Test
    fun customContentAndActionSlotsKeepOwnerSpecificCallbacks() {
        var quizChoices = 0
        var offlineStarts = 0
        compose.setContent {
            val initialFocus = remember { FocusRequester() }
            QuizzTvTheme {
                QuizzTvStatusContent(
                    actions = {
                        QuizzTvButton(
                            onClick = { offlineStarts++ },
                            modifier = Modifier.focusRequester(initialFocus),
                        ) { Text("Continue offline") }
                    },
                ) {
                    QuizzTvAnswerCard(
                        title = "Cached quiz",
                        helperText = "3 rounds",
                        state = AnswerVisualState.IDLE,
                        onClick = { quizChoices++ },
                    )
                }
            }
            LaunchedEffect(initialFocus) { initialFocus.requestFocus() }
        }

        screen.action("Continue offline").assertIsFocused()
        TvErrorRemote.press(KeyEvent.KEYCODE_DPAD_UP)
        screen.action("Cached quiz").assertIsFocused()
        TvErrorRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        compose.runOnIdle {
            assertEquals(1, quizChoices)
            assertEquals(0, offlineStarts)
        }
        TvErrorRemote.press(KeyEvent.KEYCODE_DPAD_DOWN)
        screen.action("Continue offline").assertIsFocused()
        TvErrorRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        compose.runOnIdle {
            assertEquals(1, quizChoices)
            assertEquals(1, offlineStarts)
        }
    }
}
