package com.quizzgame.feature.room.tv

import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.pressKey
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import com.quizzgame.core.designsystem.tv.preview.QuizzTvPreviewScreen
import com.quizzgame.feature.room.tv.ui.TvLobbyContent
import com.quizzgame.feature.room.tv.ui.preview.TvLobbyPreviewProvider
import com.quizzgame.feature.room.tv.ui.tags.TvLobbyTags
import com.quizzgame.shared.room.domain.entity.command.RoomCommand
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
        val sent = mutableListOf<RoomCommand>()
        val screen = TvLobbyScreenObject(compose)
        val confirmation = compose.activity.getString(R.string.room_close_description)
        compose.setContent { QuizzTvPreviewScreen { TvLobbyContent(state, {}, sent::add, {}) } }
        screen.code.assertTextEquals("031482")
        onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_DPAD_RIGHT))
        screen.close.assertIsFocused()
        onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_DPAD_CENTER))
        screen.confirmation(confirmation).assertIsDisplayed()
        onView(isRoot()).inRoot(isDialog()).perform(pressKey(KeyEvent.KEYCODE_BACK))
        screen.confirmation(confirmation).assertDoesNotExist()
        screen.code.assertIsDisplayed()
        compose.runOnIdle { assertEquals(emptyList<RoomCommand>(), sent) }
    }

    @Test
    fun playerCardOpensAConfirmationWithoutRemovingThePlayer() {
        val state = TvLobbyPreviewProvider().values.first { it.stream is RoomStreamState.Active }
        val player = requireNotNull(state.snapshot).players.last()
        val sent = mutableListOf<RoomCommand>()
        val screen = TvLobbyScreenObject(compose)
        val confirmation = compose.activity.getString(R.string.room_kick_description, player.displayName)
        compose.setContent { QuizzTvPreviewScreen { TvLobbyContent(state, {}, sent::add, {}) } }
        onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_DPAD_RIGHT))
        screen.close.assertIsFocused()
        onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_DPAD_DOWN))
        screen.player(player.id.toString()).assertIsFocused()
        onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_DPAD_CENTER))
        screen.confirmation(confirmation).assertIsDisplayed()
        onView(isRoot()).inRoot(isDialog()).perform(pressKey(KeyEvent.KEYCODE_BACK))
        screen.confirmation(confirmation).assertDoesNotExist()
        screen.player(player.id.toString()).assertIsFocused()
        compose.runOnIdle { assertEquals(emptyList<RoomCommand>(), sent) }
    }

    @Test
    fun startedScreenDoesNotShowABackButton() {
        val state = TvLobbyPreviewProvider().values.first { it.snapshot?.phase == RoomPhase.IN_GAME }
        val screen = TvLobbyScreenObject(compose)
        val back = compose.activity.getString(RoomR.string.room_back)
        compose.setContent { QuizzTvPreviewScreen { TvLobbyContent(state, {}, {}, {}) } }

        screen.confirmation(back).assertDoesNotExist()
    }
}

private class TvLobbyScreenObject(private val provider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<TvLobbyScreenObject>(provider) {
    val code = KNode(provider) { hasTestTag(TvLobbyTags.CODE) }
    val close = KNode(provider) { hasTestTag(TvLobbyTags.EXIT) }

    fun player(id: String) = KNode(provider) { hasTestTag(TvLobbyTags.player(id)) }

    fun confirmation(message: String) = KNode(provider) { hasText(message) }
}
