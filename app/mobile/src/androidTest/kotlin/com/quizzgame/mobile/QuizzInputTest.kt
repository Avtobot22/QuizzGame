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
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
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

    @Test
    fun otpPastesIntoOneAccessibleFieldAndLimitsDigits() {
        compose.setContent {
            var value by remember { mutableStateOf("") }
            QuizzMobileTheme {
                QuizzOtpInput(value = value, onValueChange = { value = it }, label = "Verification code")
            }
        }
        compose.onAllNodes(hasSetTextAction()).assertCountEquals(1)
        val field = compose.onNodeWithContentDescription("Verification code")
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
                    else -> clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
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

        compose.onNodeWithText("Code has expired").assertIsDisplayed()
        compose.onNodeWithContentDescription("Verification code")
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

        val field = compose.onNodeWithContentDescription("Verification code").assertIsNotEnabled()
        field.assert(hasSetTextAction().not()).performClick()
        field.assert(SemanticsMatcher.expectValue(SemanticsProperties.Focused, true).not())
            .assertTextEquals("123456")
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

        compose.onNode(hasSetTextAction()).performTextInput("abßı-12 cd345")
        compose.onNode(hasSetTextAction()).assert(
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

        compose.onNodeWithText("Room is unavailable", useUnmergedTree = true).assertIsDisplayed()
        compose.onNodeWithText("Enter the code on TV").assertDoesNotExist()
        compose.onNode(hasSetTextAction()).assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Error))
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

        val field = compose.onNode(
            SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("ABC123")),
        ).assertIsNotEnabled()
        field.assert(hasSetTextAction().not()).performClick()
        field.assert(SemanticsMatcher.expectValue(SemanticsProperties.Focused, true).not())
        field.assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("ABC123")))
        compose.runOnIdle { assertEquals(0, changes) }
    }
}
