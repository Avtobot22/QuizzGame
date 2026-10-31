package com.quizzgame.core.error.ui.tv

import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.designsystem.tv.QuizzTvTheme
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

    private val screen: TvErrorScreen
        get() = TvErrorScreen(compose)

    @Test
    fun latestNoticeReplacesPreviousAndDismissedEventDoesNotReplayOnRecomposition() {
        val events = ErrorNoticeEvents()
        val unrelated = mutableStateOf(0)
        val actions = mutableListOf<ErrorActionKind>()
        compose.setContent {
            val hostFocus = remember { FocusRequester() }
            QuizzTvTheme {
                Column {
                    QuizzTvButton(onClick = {}, modifier = Modifier.focusRequester(hostFocus)) {
                        Text("Saved host content")
                    }
                    Text("Owner state ${unrelated.value}")
                    QuizzTvErrorNoticeHost(events, actions::add, durationMillis = Long.MAX_VALUE)
                }
            }
            LaunchedEffect(hostFocus) { hostFocus.requestFocus() }
        }

        screen.host.assertIsFocused()
        compose.runOnIdle { events.show(spec(ErrorCopyKey.CONNECTION_PROBLEM)) }
        screen.text(copy(R.string.quizz_error_connection_problem)).assertIsDisplayed()
        screen.host.assertIsFocused()
        compose.runOnIdle { events.show(spec(ErrorCopyKey.REQUEST_TIMED_OUT)) }
        screen.text(copy(R.string.quizz_error_connection_problem)).assertDoesNotExist()
        screen.text(copy(R.string.quizz_error_request_timed_out)).assertIsDisplayed()
        screen.host.assertIsFocused()
        compose.runOnIdle { unrelated.value++ }
        screen.text(copy(R.string.quizz_error_request_timed_out)).assertIsDisplayed()
        TvErrorRemote.press(KeyEvent.KEYCODE_DPAD_DOWN)
        screen.action(copy(R.string.quizz_error_action_dismiss)).assertIsFocused()
        TvErrorRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
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
                QuizzTvTheme {
                    QuizzTvErrorNoticeHost(events, {}, durationMillis = Long.MAX_VALUE)
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
            QuizzTvTheme {
                QuizzTvErrorNoticeHost(current.value, {}, durationMillis = Long.MAX_VALUE)
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
                QuizzTvTheme {
                    QuizzTvErrorNoticeHost(events, {}, durationMillis = Long.MAX_VALUE)
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
            QuizzTvTheme {
                if (visible.value) {
                    QuizzTvErrorNoticeHost(events, {}, durationMillis = Long.MAX_VALUE)
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
