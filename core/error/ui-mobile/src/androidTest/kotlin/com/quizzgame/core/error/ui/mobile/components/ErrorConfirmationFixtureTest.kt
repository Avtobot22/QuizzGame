package com.quizzgame.core.error.ui.mobile.components

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.quizzgame.core.designsystem.mobile.QuizzConfirmationDialog
import com.quizzgame.core.designsystem.mobile.QuizzMobileTheme
import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.error.failure.ConnectivityKind
import com.quizzgame.core.error.operation.ErrorOperationContext
import com.quizzgame.core.error.operation.OperationKind
import com.quizzgame.core.error.operation.RetrySafety
import com.quizzgame.core.error.policy.DefaultErrorDecisionPolicy
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.ui.common.R
import com.quizzgame.core.error.ui.mobile.testutil.MobileErrorScreen
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
