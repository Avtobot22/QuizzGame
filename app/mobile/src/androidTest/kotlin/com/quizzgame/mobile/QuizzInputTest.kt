package com.quizzgame.mobile

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.text.AnnotatedString
import com.quizzgame.core.designsystem.mobile.QuizzMobileTheme
import com.quizzgame.core.designsystem.mobile.QuizzOtpInput
import com.quizzgame.core.designsystem.mobile.QuizzRoomCodeField
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class QuizzInputTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val screen: InputsScreen
        get() = InputsScreen(compose)

    @Test
    fun otpPastesIntoOneAccessibleFieldAndLimitsDigits() {
        compose.setContent {
            var value by remember { mutableStateOf("") }
            QuizzMobileTheme {
                QuizzOtpInput(value = value, onValueChange = { value = it }, label = "Verification code")
            }
        }
        screen.assertEditableFieldCount(1)
        val field = screen.otp
        field.performClick()

        val clipboard = compose.activity.getSystemService(ClipboardManager::class.java)
        val previousClip = clipboard.primaryClip
        try {
            compose.runOnIdle { clipboard.setPrimaryClip(ClipData.newPlainText("Code", "12a3 456789")) }
            field.performSemanticsAction(SemanticsActions.PasteText) { paste -> paste() }
            field.assertTextEquals("123456")
        } finally {
            compose.runOnIdle {
                when {
                    previousClip != null -> clipboard.setPrimaryClip(previousClip)
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> clipboard.clearPrimaryClip()
                    else -> clipboard.setPrimaryClip(
                        ClipData.newPlainText(
                            "",
                            "",
                        ),
                    )
                }
            }
        }
    }

    @Test
    fun otpErrorIsVisibleAndExposedToAccessibility() {
        compose.setContent {
            QuizzMobileTheme {
                QuizzOtpInput(
                    value = "123456",
                    onValueChange = {},
                    label = "Verification code",
                    errorMessage = "Code has expired",
                )
            }
        }

        screen.text("Code has expired").apply { assertIsDisplayed() }
        screen.otp
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Error, "Code has expired"))
    }

    @Test
    fun disabledOtpDoesNotExposeEditingOrCallOwnerOnTap() {
        var changes = 0
        compose.setContent {
            QuizzMobileTheme {
                QuizzOtpInput(
                    value = "123456",
                    onValueChange = { changes++ },
                    label = "Verification code",
                    enabled = false,
                )
            }
        }

        val field = screen.otp.apply { assertIsNotEnabled() }
        field.assert(hasSetTextAction().not())
        field.performClick()
        field.assert(SemanticsMatcher.expectValue(SemanticsProperties.Focused, true).not())
        field.assertTextEquals("123456")
        compose.runOnIdle { assertEquals(0, changes) }
    }

    @Test
    fun roomCodeInputNormalizesAndLimitsCommittedText() {
        compose.setContent {
            var value by remember { mutableStateOf("") }
            QuizzMobileTheme {
                QuizzRoomCodeField(value = value, onValueChange = { value = it }, label = "Room code")
            }
        }

        screen.editable.performTextInput("abßı-12 cd345")
        screen.editable.assert(
            SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("AB12CD")),
        )
    }

    @Test
    fun roomCodeErrorReplacesSupportingHint() {
        compose.setContent {
            QuizzMobileTheme {
                QuizzRoomCodeField(
                    value = "ABC123",
                    onValueChange = {},
                    label = "Room code",
                    supportingText = "Enter the code on TV",
                    errorMessage = "Room is unavailable",
                )
            }
        }

        screen.text("Room is unavailable", useUnmergedTree = true).apply { assertIsDisplayed() }
        screen.text("Enter the code on TV").assertDoesNotExist()
        screen.editable.assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Error))
    }

    @Test
    fun disabledRoomCodeDoesNotExposeEditingOrCallOwnerOnTap() {
        var changes = 0
        compose.setContent {
            QuizzMobileTheme {
                QuizzRoomCodeField(
                    value = "ABC123",
                    onValueChange = { changes++ },
                    label = "Room code",
                    enabled = false,
                )
            }
        }

        val field = screen.roomCodeWithValue("ABC123").apply { assertIsNotEnabled() }
        field.assert(hasSetTextAction().not())
        field.performClick()
        field.assert(SemanticsMatcher.expectValue(SemanticsProperties.Focused, true).not())
        field.assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("ABC123")))
        compose.runOnIdle { assertEquals(0, changes) }
    }
}
