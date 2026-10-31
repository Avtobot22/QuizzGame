package com.quizzgame.core.designsystem.mobile

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class QuizzConfirmationDialogTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun repeatedConfirmTapReportsOnlyOneConfirmation() {
        var confirmed = 0
        var dismissed = 0
        compose.setContent {
            QuizzMobileTheme {
                QuizzConfirmationDialog(
                    title = "Repeat operation?",
                    text = "The previous attempt may already have completed.",
                    onConfirm = { confirmed++ },
                    onDismissRequest = { dismissed++ },
                    confirmLabel = "Confirm repeat",
                    cancelLabel = "Keep current state",
                )
            }
        }

        val confirm = MobileStatusScreen(compose).action("Confirm repeat")
        confirm.performClick()
        confirm.assertIsNotEnabled()
        confirm.performClick()
        compose.runOnIdle {
            assertEquals(1, confirmed)
            assertEquals(0, dismissed)
        }
    }

    @Test
    fun cancelDismissesWithoutConfirmingDisabledOperation() {
        var confirmed = 0
        var dismissed = 0
        compose.setContent {
            QuizzMobileTheme {
                QuizzConfirmationDialog(
                    title = "Wait for operation",
                    text = "Confirmation is temporarily unavailable.",
                    onConfirm = { confirmed++ },
                    onDismissRequest = { dismissed++ },
                    confirmLabel = "Confirm repeat",
                    cancelLabel = "Cancel",
                    enabled = false,
                )
            }
        }

        MobileStatusScreen(compose).action("Confirm repeat").assertIsNotEnabled()
        MobileStatusScreen(compose).action("Cancel").performClick()
        compose.runOnIdle {
            assertEquals(0, confirmed)
            assertEquals(1, dismissed)
        }
    }
}
