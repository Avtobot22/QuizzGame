package com.quizzgame.core.designsystem.tv

import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.tv.material3.Text
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class QuizzTvConfirmationDialogTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun dialogStartsOnCancelAndRepeatedCenterConfirmsOnlyOnce() {
        var confirmed = 0
        var dismissed = 0
        compose.setContent {
            QuizzTvTheme {
                QuizzTvConfirmationDialog(
                    title = "Repeat operation?",
                    text = "The previous attempt may already have completed.",
                    onConfirm = { confirmed++ },
                    onDismissRequest = { dismissed++ },
                    confirmLabel = "Confirm repeat",
                    cancelLabel = "Cancel",
                )
            }
        }

        val screen = TvStatusScreen(compose)
        screen.action("Cancel").assertIsFocused()
        TvErrorRemote.press(KeyEvent.KEYCODE_DPAD_RIGHT)
        screen.action("Confirm repeat").assertIsFocused()
        TvErrorRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        TvErrorRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        compose.runOnIdle {
            assertEquals(1, confirmed)
            assertEquals(0, dismissed)
        }
    }

    @Test
    fun backClosesDialogBeforeOwnerNavigationAndRestoresTriggerFocus() {
        val visible = mutableStateOf(false)
        var confirmed = 0
        var dismissed = 0
        var ownerBack = 0
        compose.setContent {
            val triggerFocus = remember { FocusRequester() }
            BackHandler { ownerBack++ }
            QuizzTvTheme {
                QuizzTvButton(
                    onClick = { visible.value = true },
                    modifier = Modifier.focusRequester(triggerFocus),
                ) { Text("Open confirmation") }
                if (visible.value) {
                    QuizzTvConfirmationDialog(
                        title = "Confirm operation",
                        text = "The owner controls the operation.",
                        onConfirm = { confirmed++ },
                        onDismissRequest = {
                            dismissed++
                            visible.value = false
                        },
                        confirmLabel = "Confirm",
                        cancelLabel = "Cancel",
                        returnFocusRequester = triggerFocus,
                    )
                }
            }
            LaunchedEffect(triggerFocus) { triggerFocus.requestFocus() }
        }

        val screen = TvStatusScreen(compose)
        screen.action("Open confirmation").assertIsFocused()
        TvErrorRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        screen.action("Cancel").assertIsFocused()
        TvErrorRemote.press(KeyEvent.KEYCODE_BACK)
        screen.text("Confirm operation").assertDoesNotExist()
        screen.action("Open confirmation").assertIsFocused()
        compose.runOnIdle {
            assertEquals(0, confirmed)
            assertEquals(1, dismissed)
            assertEquals(0, ownerBack)
        }
    }
}
