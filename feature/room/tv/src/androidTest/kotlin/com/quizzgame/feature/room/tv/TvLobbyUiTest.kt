package com.quizzgame.feature.room.tv

import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.pressKey
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import com.quizzgame.core.designsystem.tv.preview.QuizzTvPreviewScreen
import com.quizzgame.feature.room.tv.presentation.TvLobbyIntent
import com.quizzgame.feature.room.tv.ui.TvLobbyContent
import com.quizzgame.feature.room.tv.ui.preview.TvLobbyPreviewProvider
import com.quizzgame.feature.room.tv.ui.tags.TvLobbyTags
import com.quizzgame.shared.room.domain.entity.session.RoomStreamState
import com.quizzgame.shared.room.domain.entity.snapshot.RoomPhase
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import com.quizzgame.shared.room.R as RoomR

class TvLobbyUiTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun pinKeepsLeadingZeroAndCloseIsConfirmed() {
        val state = TvLobbyPreviewProvider().values.first { it.stream is RoomStreamState.Active }
        val sent = mutableListOf<TvLobbyIntent>()
        val screen = TvLobbyScreenObject(compose)
        val confirmation = compose.activity.getString(R.string.room_close_description)
        showLobby { TvLobbyContent(state, {}, sent::add) }
        screen.code.assertTextEquals("031482")
        onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_DPAD_RIGHT))
        screen.close.assertIsFocused()
        onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_DPAD_CENTER))
        screen.confirmation(confirmation).assertIsDisplayed()
        onView(isRoot()).inRoot(isDialog()).perform(pressKey(KeyEvent.KEYCODE_BACK))
        screen.confirmation(confirmation).assertDoesNotExist()
        screen.code.assertIsDisplayed()
        compose.runOnIdle { assertEquals(emptyList<TvLobbyIntent>(), sent) }
    }

    @Test
    fun playerCardOpensAConfirmationWithoutRemovingThePlayer() {
        val state = TvLobbyPreviewProvider().values.first { it.stream is RoomStreamState.Active }
        val player = requireNotNull(state.snapshot).players.last()
        val sent = mutableListOf<TvLobbyIntent>()
        val screen = TvLobbyScreenObject(compose)
        val confirmation = compose.activity.getString(R.string.room_kick_description, player.displayName)
        showLobby { TvLobbyContent(state, {}, sent::add) }
        onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_DPAD_RIGHT))
        screen.close.assertIsFocused()
        onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_DPAD_DOWN))
        screen.player(player.id.toString()).assertIsFocused()
        onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_DPAD_CENTER))
        screen.confirmation(confirmation).assertIsDisplayed()
        onView(isRoot()).inRoot(isDialog()).perform(pressKey(KeyEvent.KEYCODE_BACK))
        screen.confirmation(confirmation).assertDoesNotExist()
        screen.player(player.id.toString()).assertIsFocused()
        compose.runOnIdle { assertEquals(emptyList<TvLobbyIntent>(), sent) }
    }

    @Test
    fun startedScreenDoesNotShowABackButton() {
        val state = TvLobbyPreviewProvider().values.first { it.snapshot?.phase == RoomPhase.IN_GAME }
        val screen = TvLobbyScreenObject(compose)
        val back = compose.activity.getString(RoomR.string.room_back)
        showLobby { TvLobbyContent(state, {}, {}) }

        screen.confirmation(back).assertDoesNotExist()
    }

    @Test
    fun keepsCommandFeedbackVisibleDuringReconnectAndEnablesRetryAfterwards() {
        val reconnecting = TvLobbyPreviewProvider().values.first {
            it.stream is RoomStreamState.Reconnecting && it.commandError != null
        }
        val state = mutableStateOf(reconnecting)
        val screen = TvLobbyScreenObject(compose)
        val title = compose.activity.getString(RoomR.string.room_kick_failed)
        val connection = compose.activity.getString(RoomR.string.room_reconnecting)
        var retried = false
        showLobby { TvLobbyContent(state.value, {}) { retried = it == TvLobbyIntent.Retry } }

        screen.confirmation(title).assertIsDisplayed()
        screen.confirmation(connection).assertIsDisplayed()
        screen.retry.assertDoesNotExist()
        compose.runOnIdle {
            state.value = reconnecting.copy(stream = RoomStreamState.Active(requireNotNull(reconnecting.snapshot)))
        }
        onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_DPAD_RIGHT))
        onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_DPAD_DOWN))
        onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_DPAD_DOWN))
        screen.retry.assertIsFocused()
        onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_DPAD_CENTER))
        compose.runOnIdle { assertEquals(true, retried) }
    }

    private fun showLobby(content: @Composable () -> Unit) {
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                QuizzTvPreviewScreen(Modifier.size(960.dp, 540.dp)) { content() }
            }
        }
    }
}

private class TvLobbyScreenObject(private val provider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<TvLobbyScreenObject>(provider) {
    val code = KNode(provider) { hasTestTag(TvLobbyTags.CODE) }
    val close = KNode(provider) { hasTestTag(TvLobbyTags.EXIT) }
    val retry = KNode(provider) { hasTestTag(TvLobbyTags.RETRY) }

    fun player(id: String) = KNode(provider) { hasTestTag(TvLobbyTags.player(id)) }

    fun confirmation(message: String) = KNode(provider) { hasText(message) }
}
