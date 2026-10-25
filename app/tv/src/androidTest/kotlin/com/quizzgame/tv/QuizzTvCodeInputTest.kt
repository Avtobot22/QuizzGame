package com.quizzgame.tv

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.designsystem.tv.QuizzTvCodeInput
import com.quizzgame.core.designsystem.tv.QuizzTvTheme
import com.quizzgame.core.ui.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import android.view.KeyEvent as AndroidKeyEvent

class QuizzTvCodeInputTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val screen: TvCodeScreen
        get() = TvCodeScreen(compose)

    @Test
    fun remoteMovesBetweenDigitsWithoutEnteringThemUntilCenterIsPressed() {
        val fixture = showInput()

        screen.character("1").assertIsFocused()
        TvRemote.press(AndroidKeyEvent.KEYCODE_DPAD_RIGHT)
        screen.character("2").assertIsFocused()
        compose.runOnIdle { assertEquals("", fixture.value) }

        TvRemote.press(AndroidKeyEvent.KEYCODE_DPAD_CENTER)
        compose.runOnIdle { assertEquals("2", fixture.value) }
    }

    @Test
    fun returningFromAnExternalActionRestoresThePreviouslyFocusedDigit() {
        val fixture = CodeFixture(initialValue = "12", errorMessage = null)
        compose.setContent {
            QuizzTvTheme {
                Column(Modifier.fillMaxWidth()) {
                    fixture.Content(Modifier)
                    QuizzTvButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                        Text("Resend code")
                    }
                }
            }
        }

        screen.character("1").assertIsFocused()
        TvRemote.press(AndroidKeyEvent.KEYCODE_DPAD_RIGHT)
        screen.character("2").assertIsFocused()
        TvRemote.press(AndroidKeyEvent.KEYCODE_DPAD_RIGHT)
        screen.character("3").assertIsFocused()
        TvRemote.press(AndroidKeyEvent.KEYCODE_DPAD_DOWN)
        screen.resend.assertIsFocused()
        TvRemote.press(AndroidKeyEvent.KEYCODE_DPAD_UP)

        screen.character("3").assertIsFocused()
        compose.runOnIdle {
            assertEquals("12", fixture.value)
            assertEquals(emptyList<String>(), fixture.changes)
        }
    }

    @Test
    fun physicalDigitsCompleteCodeOnceAndRecompositionDoesNotSubmitAgain() {
        val fixture = showInput()

        listOf(
            AndroidKeyEvent.KEYCODE_1,
            AndroidKeyEvent.KEYCODE_2,
            AndroidKeyEvent.KEYCODE_3,
            AndroidKeyEvent.KEYCODE_4,
            AndroidKeyEvent.KEYCODE_5,
            AndroidKeyEvent.KEYCODE_6,
        ).forEach { key ->
            TvRemote.press(key)
        }
        compose.runOnIdle {
            assertEquals("123456", fixture.value)
            assertEquals(listOf("123456"), fixture.completions)
            fixture.errorMessage = "Try another code"
        }

        screen.input.assert(
            SemanticsMatcher.expectValue(SemanticsProperties.Error, "Try another code"),
        )
        compose.runOnIdle { assertEquals(listOf("123456"), fixture.completions) }
    }

    @Test
    fun rapidHardwareDigitsBeforeTheNextFrameCompleteTheWholeCode() {
        val fixture = showInput()

        TvRemote.pressBeforeNextFrame(
            AndroidKeyEvent.KEYCODE_1,
            AndroidKeyEvent.KEYCODE_2,
            AndroidKeyEvent.KEYCODE_3,
            AndroidKeyEvent.KEYCODE_4,
            AndroidKeyEvent.KEYCODE_5,
            AndroidKeyEvent.KEYCODE_6,
        )

        compose.runOnIdle {
            assertEquals("123456", fixture.value)
            assertEquals(listOf("123456"), fixture.completions)
        }
    }

    @Test
    fun rapidBackspaceAndRefillCompleteTheEditedCode() {
        val fixture = showInput(initialValue = "123456")

        TvRemote.pressBeforeNextFrame(AndroidKeyEvent.KEYCODE_DEL, AndroidKeyEvent.KEYCODE_NUMPAD_0)

        compose.runOnIdle {
            assertEquals("123450", fixture.value)
            assertEquals(listOf("12345", "123450"), fixture.changes)
            assertEquals(listOf("123450"), fixture.completions)
        }
    }

    @Test
    fun callerClearingCodeDiscardsPendingHardwareDigits() {
        val fixture = showInput()
        TvRemote.pressBeforeNextFrame(AndroidKeyEvent.KEYCODE_1, AndroidKeyEvent.KEYCODE_2)
        compose.runOnIdle {
            assertEquals("12", fixture.value)
            fixture.value = ""
        }
        compose.waitForIdle()

        TvRemote.pressBeforeNextFrame(AndroidKeyEvent.KEYCODE_3, AndroidKeyEvent.KEYCODE_4)

        compose.runOnIdle {
            assertEquals("34", fixture.value)
            assertEquals(emptyList<String>(), fixture.completions)
        }
    }

    @Test
    fun fullCodeIgnoresOverflowAndNonDigitKeys() {
        val fixture = showInput(initialValue = "123456")

        listOf(AndroidKeyEvent.KEYCODE_7, AndroidKeyEvent.KEYCODE_A, AndroidKeyEvent.KEYCODE_NUMPAD_8).forEach { key ->
            TvRemote.press(key)
        }

        compose.runOnIdle {
            assertEquals("123456", fixture.value)
            assertEquals(emptyList<String>(), fixture.changes)
            assertEquals(emptyList<String>(), fixture.completions)
        }
    }

    @Test
    fun deleteActionRemovesLastDigitAndRefillCompletesEditedCode() {
        val fixture = showInput(initialValue = "123456")

        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0").forEach { digit ->
            screen.character(digit).assertIsFocused()
            TvRemote.press(AndroidKeyEvent.KEYCODE_DPAD_RIGHT)
        }
        screen.delete.assertIsFocused()
        TvRemote.press(AndroidKeyEvent.KEYCODE_DPAD_CENTER)
        compose.runOnIdle { assertEquals("12345", fixture.value) }

        TvRemote.press(AndroidKeyEvent.KEYCODE_NUMPAD_0)
        compose.runOnIdle {
            assertEquals("123450", fixture.value)
            assertEquals(listOf("123450"), fixture.completions)
        }
    }

    @Test
    fun physicalBackspaceRemovesOneDigitAndDoesNothingWhenEmpty() {
        val fixture = showInput(initialValue = "12")

        listOf("1", "", "").forEach { expected ->
            TvRemote.press(AndroidKeyEvent.KEYCODE_DEL)
            compose.runOnIdle { assertEquals(expected, fixture.value) }
        }
        compose.runOnIdle { assertEquals(listOf("1", ""), fixture.changes) }
    }

    @Test
    fun disabledInputIgnoresKeysAndPreservesFocusedDigitWhenEnabledAgain() {
        val fixture = showInput()
        TvRemote.press(AndroidKeyEvent.KEYCODE_DPAD_RIGHT)
        screen.character("2").assertIsFocused()
        compose.runOnIdle { fixture.enabled = false }

        screen.character("2").apply {
            assertIsNotEnabled()
            assertIsFocused()
        }
        TvRemote.press(AndroidKeyEvent.KEYCODE_DPAD_CENTER)
        TvRemote.press(AndroidKeyEvent.KEYCODE_5)
        compose.runOnIdle {
            assertEquals("", fixture.value)
            fixture.enabled = true
        }

        screen.character("2").assertIsFocused()
        TvRemote.press(AndroidKeyEvent.KEYCODE_DPAD_CENTER)
        compose.runOnIdle { assertEquals("2", fixture.value) }
    }

    @Test
    fun codeCellsDoNotReceiveFocusOrExposeTheCodeThroughTextSemantics() {
        showInput(initialValue = "123456", errorMessage = "The code has expired")

        screen.assertFocusableCount(11)
        screen.input.assert(
            SemanticsMatcher.expectValue(SemanticsProperties.Error, "The code has expired"),
        )
        listOf("1", "2", "3", "4", "5", "6").forEach { digit ->
            screen.assertUnmergedTextCount(digit, 1)
        }
        screen.assertUnmergedTextCount("123456", 0)
        screen.character("1").assertIsFocused()
    }

    @Test
    fun backKeyReachesTheCallerWithoutChangingCode() {
        var backEvents = 0
        val fixture = showInput(
            initialValue = "12",
            modifier = Modifier.onKeyEvent { event ->
                if (event.key == Key.Back && event.type == KeyEventType.KeyUp) {
                    backEvents++
                    true
                } else {
                    false
                }
            },
        )

        TvRemote.pressBeforeNextFrame(AndroidKeyEvent.KEYCODE_BACK)

        compose.runOnIdle {
            assertEquals(1, backEvents)
            assertEquals("12", fixture.value)
        }
    }

    @Test
    fun callerDefinedLengthCompletesAtFourCharacters() {
        val fixture = showInput(length = 4, label = "Invite code")
        val inviteScreen = TvCodeScreen(compose, label = "Invite code")

        TvRemote.pressBeforeNextFrame(
            AndroidKeyEvent.KEYCODE_1,
            AndroidKeyEvent.KEYCODE_2,
            AndroidKeyEvent.KEYCODE_3,
            AndroidKeyEvent.KEYCODE_4,
            AndroidKeyEvent.KEYCODE_5,
        )

        inviteScreen.input.assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.StateDescription,
                compose.activity.getString(R.string.quizz_tv_code_progress, 4, 4),
            ),
        )
        compose.runOnIdle {
            assertEquals("1234", fixture.value)
            assertEquals(listOf("1234"), fixture.completions)
        }
    }

    @Test
    fun callerDefinedCharactersSupportRemoteAndHardwareLetters() {
        val fixture = showInput(length = 4, characters = "ab12")

        screen.assertFocusableCount(5)
        screen.character("a").assertIsFocused()
        screen.character("c").assertDoesNotExist()
        TvRemote.press(AndroidKeyEvent.KEYCODE_DPAD_RIGHT)
        screen.character("b").assertIsFocused()
        TvRemote.press(AndroidKeyEvent.KEYCODE_DPAD_CENTER)
        TvRemote.pressBeforeNextFrame(
            AndroidKeyEvent.KEYCODE_A,
            AndroidKeyEvent.KEYCODE_C,
            AndroidKeyEvent.KEYCODE_NUMPAD_1,
            AndroidKeyEvent.KEYCODE_2,
        )

        compose.runOnIdle {
            assertEquals("ba12", fixture.value)
            assertEquals(listOf("b", "ba", "ba1", "ba12"), fixture.changes)
            assertEquals(listOf("ba12"), fixture.completions)
        }
    }

    @Test
    fun changingLengthAndCharactersDiscardsEditsFromThePreviousConfiguration() {
        val fixture = showInput()
        TvRemote.pressBeforeNextFrame(AndroidKeyEvent.KEYCODE_1, AndroidKeyEvent.KEYCODE_2)
        compose.runOnIdle {
            assertEquals("12", fixture.value)
            fixture.length = 2
            fixture.characters = "ab"
            fixture.changes.clear()
        }
        compose.waitForIdle()

        screen.input.assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.StateDescription,
                compose.activity.getString(R.string.quizz_tv_code_progress, 0, 2),
            ),
        )
        compose.runOnIdle {
            assertEquals("12", fixture.value)
            assertEquals(emptyList<String>(), fixture.changes)
            assertEquals(emptyList<String>(), fixture.completions)
        }
        TvRemote.pressBeforeNextFrame(
            AndroidKeyEvent.KEYCODE_1,
            AndroidKeyEvent.KEYCODE_A,
            AndroidKeyEvent.KEYCODE_B,
        )

        compose.runOnIdle {
            assertEquals("ab", fixture.value)
            assertEquals(listOf("a", "ab"), fixture.changes)
            assertEquals(listOf("ab"), fixture.completions)
        }
    }

    private fun showInput(
        initialValue: String = "",
        errorMessage: String? = null,
        modifier: Modifier = Modifier,
        length: Int = 6,
        characters: String = "1234567890",
        label: String = "Room code",
    ): CodeFixture {
        val fixture = CodeFixture(initialValue, errorMessage, length, characters, label)
        compose.setContent { fixture.Content(modifier) }
        return fixture
    }
}

private class CodeFixture(
    initialValue: String,
    errorMessage: String?,
    length: Int = 6,
    characters: String = "1234567890",
    private val label: String = "Room code",
) {
    var value by mutableStateOf(initialValue)
    var enabled by mutableStateOf(true)
    var errorMessage by mutableStateOf(errorMessage)
    var length by mutableStateOf(length)
    var characters by mutableStateOf(characters)
    val changes = mutableListOf<String>()
    val completions = mutableListOf<String>()

    @Composable
    fun Content(modifier: Modifier) {
        QuizzTvTheme {
            QuizzTvCodeInput(
                value = value,
                onValueChange = {
                    changes += it
                    value = it
                },
                onComplete = { completions += it },
                length = length,
                characters = characters,
                label = label,
                modifier = modifier,
                enabled = enabled,
                isError = errorMessage != null,
                errorMessage = errorMessage,
            )
        }
    }
}
