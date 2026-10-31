package com.quizzgame.core.error.ui.mobile

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.quizzgame.core.designsystem.mobile.QuizzConfirmationDialog
import com.quizzgame.core.designsystem.mobile.QuizzMobileTheme
import com.quizzgame.core.error.AppFailure
import com.quizzgame.core.error.ConnectivityKind
import com.quizzgame.core.error.DefaultErrorDecisionPolicy
import com.quizzgame.core.error.ErrorActionKind
import com.quizzgame.core.error.ErrorOperationContext
import com.quizzgame.core.error.OperationKind
import com.quizzgame.core.error.RetrySafety
import com.quizzgame.core.error.ui.common.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ErrorConfirmationFixtureTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun ambiguousOperationRunsOnlyAfterOwnerShowsAndUserAcceptsConfirmation() {
        val showingConfirmation = mutableStateOf(false)
        var operationCalls = 0
        val spec = DefaultErrorDecisionPolicy().decide(
            AppFailure.Connectivity(ConnectivityKind.TIMEOUT),
            ErrorOperationContext(OperationKind.INITIAL_LOAD, false, RetrySafety.REQUIRES_CONFIRMATION),
        )
        compose.setContent {
            QuizzMobileTheme {
                QuizzMobileErrorFullScreen(
                    spec,
                    onAction = { action ->
                        if (action == ErrorActionKind.CONFIRM_RETRY) showingConfirmation.value = true
                    },
                )
                if (showingConfirmation.value) {
                    QuizzConfirmationDialog(
                        title = "The request may have completed",
                        text = "Check the previous result before repeating the operation.",
                        confirmLabel = "Repeat anyway",
                        cancelLabel = "Keep current state",
                        onConfirm = {
                            operationCalls++
                            showingConfirmation.value = false
                        },
                        onDismissRequest = { showingConfirmation.value = false },
                    )
                }
            }
        }

        val screen = MobileErrorScreen(compose)
        screen.action(compose.activity.getString(R.string.quizz_error_action_confirm_retry)).performClick()
        screen.text("The request may have completed").assertIsDisplayed()
        compose.runOnIdle { assertEquals(0, operationCalls) }
        screen.action("Repeat anyway").performClick()
        compose.runOnIdle { assertEquals(1, operationCalls) }
    }
}
