package com.quizzgame.core.designsystem.mobile

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class QuizzMobileStatusContentTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val screen: MobileStatusScreen
        get() = MobileStatusScreen(compose)

    @Test
    fun customContentAndActionSlotsCanEditAndSubmitAnOwnerDraft() {
        val draft = mutableStateOf("")
        var submitted = ""
        compose.setContent {
            QuizzMobileTheme {
                QuizzStatusContent(
                    actions = {
                        QuizzButton(onClick = { submitted = draft.value }) { Text("Keep draft") }
                    },
                ) {
                    Text("Room could not be opened")
                    TextField(value = draft.value, onValueChange = { draft.value = it })
                }
            }
        }

        screen.input.performTextInput("ROOM42")
        screen.action("Keep draft").performClick()
        compose.runOnIdle { assertEquals("ROOM42", submitted) }
    }
}
