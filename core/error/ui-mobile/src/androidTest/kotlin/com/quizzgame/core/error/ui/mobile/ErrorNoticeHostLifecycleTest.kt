package com.quizzgame.core.error.ui.mobile

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.quizzgame.core.designsystem.mobile.QuizzMobileTheme
import com.quizzgame.core.error.ErrorActionKind
import com.quizzgame.core.error.ErrorCopyKey
import com.quizzgame.core.error.ErrorPresentationSpec
import com.quizzgame.core.error.ErrorSurface
import com.quizzgame.core.error.ui.common.ErrorNoticeEvents
import com.quizzgame.core.error.ui.common.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ErrorNoticeHostLifecycleTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val screen: MobileErrorScreen
        get() = MobileErrorScreen(compose)

    @Test
    fun latestNoticeReplacesPreviousAndDismissedEventDoesNotReplayOnRecomposition() {
        val events = ErrorNoticeEvents()
        val unrelated = mutableStateOf(0)
        val actions = mutableListOf<ErrorActionKind>()
        compose.setContent {
            QuizzMobileTheme {
                Text("Owner state ${unrelated.value}")
                QuizzMobileErrorNoticeHost(events, actions::add, durationMillis = Long.MAX_VALUE)
            }
        }

        compose.runOnIdle { events.show(spec(ErrorCopyKey.CONNECTION_PROBLEM)) }
        screen.text(copy(R.string.quizz_error_connection_problem)).assertIsDisplayed()
        compose.runOnIdle { events.show(spec(ErrorCopyKey.REQUEST_TIMED_OUT)) }
        screen.text(copy(R.string.quizz_error_connection_problem)).assertDoesNotExist()
        screen.text(copy(R.string.quizz_error_request_timed_out)).assertIsDisplayed()
        screen.action(copy(R.string.quizz_error_action_dismiss)).performClick()
        compose.runOnIdle { unrelated.value++ }
        screen.text(copy(R.string.quizz_error_request_timed_out)).assertDoesNotExist()
        compose.runOnIdle { assertEquals(listOf(ErrorActionKind.DISMISS), actions) }
    }

    @Test
    fun lifecycleStopClearsVisibleNoticeAndDropsEventsUntilStarted() {
        val owner = NoticeTestLifecycleOwner()
        val events = ErrorNoticeEvents()
        compose.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                QuizzMobileTheme {
                    QuizzMobileErrorNoticeHost(events, {}, durationMillis = Long.MAX_VALUE)
                }
            }
        }
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.STARTED }
        compose.runOnIdle { events.show(spec(ErrorCopyKey.CONNECTION_PROBLEM)) }
        screen.text(copy(R.string.quizz_error_connection_problem)).assertIsDisplayed()
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.CREATED }
        screen.text(copy(R.string.quizz_error_connection_problem)).assertDoesNotExist()
        compose.runOnIdle { events.show(spec(ErrorCopyKey.REQUEST_TIMED_OUT)) }
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.STARTED }
        screen.text(copy(R.string.quizz_error_connection_problem)).assertDoesNotExist()
        screen.text(copy(R.string.quizz_error_request_timed_out)).assertDoesNotExist()
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.DESTROYED }
    }

    @Test
    fun replacingScreenEventSourceDiscardsOldNoticeAndOldOwnerEvents() {
        val previous = ErrorNoticeEvents()
        val next = ErrorNoticeEvents()
        val current = mutableStateOf(previous)
        compose.setContent {
            QuizzMobileTheme {
                QuizzMobileErrorNoticeHost(current.value, {}, durationMillis = Long.MAX_VALUE)
            }
        }

        compose.runOnIdle { previous.show(spec(ErrorCopyKey.CONNECTION_PROBLEM)) }
        screen.text(copy(R.string.quizz_error_connection_problem)).assertIsDisplayed()
        compose.runOnIdle { current.value = next }
        screen.text(copy(R.string.quizz_error_connection_problem)).assertDoesNotExist()
        compose.runOnIdle { previous.show(spec(ErrorCopyKey.REQUEST_TIMED_OUT)) }
        screen.text(copy(R.string.quizz_error_request_timed_out)).assertDoesNotExist()
        compose.runOnIdle { next.show(spec(ErrorCopyKey.SERVER_UNAVAILABLE)) }
        screen.text(copy(R.string.quizz_error_server_unavailable)).assertIsDisplayed()
    }

    @Test
    fun replacingLifecycleOwnerClearsNoticeAndDetachesPreviousOwner() {
        val previous = NoticeTestLifecycleOwner()
        val next = NoticeTestLifecycleOwner()
        val owner = mutableStateOf(previous)
        val events = ErrorNoticeEvents()
        compose.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner.value) {
                QuizzMobileTheme {
                    QuizzMobileErrorNoticeHost(events, {}, durationMillis = Long.MAX_VALUE)
                }
            }
        }

        compose.runOnIdle { previous.registry.currentState = Lifecycle.State.STARTED }
        compose.runOnIdle { events.show(spec(ErrorCopyKey.CONNECTION_PROBLEM)) }
        screen.text(copy(R.string.quizz_error_connection_problem)).assertIsDisplayed()
        compose.runOnIdle { owner.value = next }
        screen.text(copy(R.string.quizz_error_connection_problem)).assertDoesNotExist()
        compose.runOnIdle { events.show(spec(ErrorCopyKey.REQUEST_TIMED_OUT)) }
        compose.runOnIdle { next.registry.currentState = Lifecycle.State.STARTED }
        screen.text(copy(R.string.quizz_error_request_timed_out)).assertDoesNotExist()
        compose.runOnIdle { events.show(spec(ErrorCopyKey.SERVER_UNAVAILABLE)) }
        screen.text(copy(R.string.quizz_error_server_unavailable)).assertIsDisplayed()
        compose.runOnIdle { previous.registry.currentState = Lifecycle.State.DESTROYED }
        screen.text(copy(R.string.quizz_error_server_unavailable)).assertIsDisplayed()
        compose.runOnIdle { next.registry.currentState = Lifecycle.State.DESTROYED }
    }

    @Test
    fun removingAndRemountingHostDoesNotReplayVisibleOrMissedEvents() {
        val events = ErrorNoticeEvents()
        val visible = mutableStateOf(true)
        compose.setContent {
            QuizzMobileTheme {
                if (visible.value) {
                    QuizzMobileErrorNoticeHost(events, {}, durationMillis = Long.MAX_VALUE)
                }
            }
        }

        compose.runOnIdle { events.show(spec(ErrorCopyKey.CONNECTION_PROBLEM)) }
        screen.text(copy(R.string.quizz_error_connection_problem)).assertIsDisplayed()
        compose.runOnIdle { visible.value = false }
        screen.text(copy(R.string.quizz_error_connection_problem)).assertDoesNotExist()
        compose.runOnIdle { events.show(spec(ErrorCopyKey.REQUEST_TIMED_OUT)) }
        compose.runOnIdle { visible.value = true }
        screen.text(copy(R.string.quizz_error_connection_problem)).assertDoesNotExist()
        screen.text(copy(R.string.quizz_error_request_timed_out)).assertDoesNotExist()
        compose.runOnIdle { events.show(spec(ErrorCopyKey.SERVER_UNAVAILABLE)) }
        screen.text(copy(R.string.quizz_error_server_unavailable)).assertIsDisplayed()
    }

    private fun copy(resource: Int): String = compose.activity.getString(resource)

    private fun spec(title: ErrorCopyKey) = ErrorPresentationSpec(
        surface = ErrorSurface.TRANSIENT,
        title = title,
        actions = listOf(ErrorActionKind.DISMISS),
    )
}

private class NoticeTestLifecycleOwner : LifecycleOwner {
    val registry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle = registry
}
