package com.quizzgame.tv

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.AnswerVisualState
import com.quizzgame.core.designsystem.tv.PairingState
import com.quizzgame.core.designsystem.tv.QuizzTvAnswerCard
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.designsystem.tv.QuizzTvPairingPanel
import com.quizzgame.core.designsystem.tv.QuizzTvTheme
import com.quizzgame.core.ui.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import android.view.KeyEvent as AndroidKeyEvent

class QuizzTvComponentTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val screen: TvComponentScreen
        get() = TvComponentScreen(compose)

    @Test
    fun movingFocusDoesNotChangeAnswerSelectionOrActivateIt() {
        var activations = 0
        compose.setContent {
            val initialFocus = remember { FocusRequester() }
            QuizzTvTheme {
                Column {
                    QuizzTvAnswerCard(
                        title = "First answer",
                        helperText = "A",
                        state = AnswerVisualState.IDLE,
                        onClick = { activations++ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(initialFocus),
                    )
                    QuizzTvAnswerCard(
                        title = "Chosen answer",
                        helperText = "B",
                        state = AnswerVisualState.SELECTED,
                        onClick = { activations++ },
                    )
                }
            }
            LaunchedEffect(initialFocus) { initialFocus.requestFocus() }
        }

        screen.text("First answer").apply {
            assertIsFocused()
            assertIsNotSelected()
        }
        screen.text("Chosen answer").apply {
            assertIsNotFocused()
            assertIsSelected()
        }
        TvRemote.press(AndroidKeyEvent.KEYCODE_DPAD_DOWN)
        screen.text("Chosen answer").apply {
            assertIsFocused()
            assertIsSelected()
        }
        screen.text("First answer").apply {
            assertIsNotFocused()
            assertIsNotSelected()
        }
        compose.runOnIdle { assertEquals(0, activations) }
    }

    @Test
    fun centerKeyActivatesFocusedAnswerOnce() {
        var activations = 0
        compose.setContent {
            val initialFocus = remember { FocusRequester() }
            QuizzTvTheme {
                QuizzTvAnswerCard(
                    title = "Answer",
                    helperText = "A",
                    state = AnswerVisualState.IDLE,
                    onClick = { activations++ },
                    modifier = Modifier.focusRequester(initialFocus),
                )
            }
            LaunchedEffect(initialFocus) { initialFocus.requestFocus() }
        }

        screen.text("Answer").assertIsFocused()
        TvRemote.press(AndroidKeyEvent.KEYCODE_DPAD_CENTER)
        compose.runOnIdle { assertEquals(1, activations) }
    }

    @Test
    fun disabledAnswerIgnoresCenterKey() {
        var activations = 0
        compose.setContent {
            val initialFocus = remember { FocusRequester() }
            QuizzTvTheme {
                QuizzTvAnswerCard(
                    title = "Unavailable answer",
                    helperText = "A",
                    state = AnswerVisualState.DISABLED,
                    onClick = { activations++ },
                    modifier = Modifier.focusRequester(initialFocus),
                )
            }
            LaunchedEffect(initialFocus) { initialFocus.requestFocus() }
        }

        screen.text("Unavailable answer").apply {
            assertIsNotEnabled()
            assertIsFocused()
        }
        TvRemote.press(AndroidKeyEvent.KEYCODE_DPAD_CENTER)
        compose.runOnIdle { assertEquals(0, activations) }
    }

    @Test
    fun answerFeedbackIsExposedWithoutDependingOnColor() {
        compose.setContent {
            QuizzTvTheme {
                Column {
                    QuizzTvAnswerCard(
                        title = "Correct answer",
                        helperText = "A",
                        state = AnswerVisualState.CORRECT,
                        onClick = {},
                    )
                    QuizzTvAnswerCard(
                        title = "Wrong answer",
                        helperText = "B",
                        state = AnswerVisualState.WRONG,
                        onClick = {},
                    )
                }
            }
        }

        listOf("Correct answer" to R.string.quizz_answer_correct, "Wrong answer" to R.string.quizz_answer_wrong)
            .forEach { (title, description) ->
                screen.text(title).assert(
                    SemanticsMatcher.expectValue(
                        SemanticsProperties.StateDescription,
                        compose.activity.getString(description),
                    ),
                )
            }
    }

    @Test
    fun panelContentStaysOutsideFocusAndRemoteTraversesActions() {
        var cancelled = 0
        compose.setContent {
            val initialFocus = remember { FocusRequester() }
            QuizzTvTheme {
                QuizzTvPairingPanel(
                    state = PairingState.WAITING,
                    title = "Join room",
                    code = "ROOM42",
                    supportingText = "Expires in 02:00",
                    qrContent = { Text("QR image") },
                    actions = {
                        Row {
                            QuizzTvButton(onClick = {}, modifier = Modifier.focusRequester(initialFocus)) {
                                Text("Refresh")
                            }
                            QuizzTvButton(onClick = { cancelled++ }) { Text("Cancel") }
                        }
                    },
                )
            }
            LaunchedEffect(initialFocus) { initialFocus.requestFocus() }
        }

        val hasFocusState = SemanticsMatcher.keyIsDefined(SemanticsProperties.Focused)
        screen.assertFocusableCount(2)
        listOf("QR image", "ROOM42", "Expires in 02:00").forEach { text ->
            screen.text(text, useUnmergedTree = true).assert(hasFocusState.not())
        }
        screen.text("Refresh").assertIsFocused()
        TvRemote.press(AndroidKeyEvent.KEYCODE_DPAD_RIGHT)
        screen.text("Cancel").assertIsFocused()
        TvRemote.press(AndroidKeyEvent.KEYCODE_DPAD_CENTER)
        compose.runOnIdle { assertEquals(1, cancelled) }
    }
}
