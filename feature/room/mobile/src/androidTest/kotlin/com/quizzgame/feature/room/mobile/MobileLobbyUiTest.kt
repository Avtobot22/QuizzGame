package com.quizzgame.feature.room.mobile

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.espresso.Espresso.pressBack
import com.quizzgame.core.designsystem.mobile.preview.QuizzMobilePreviewScreen
import com.quizzgame.feature.room.mobile.ui.MobileLobbyContent
import com.quizzgame.feature.room.mobile.ui.preview.MobileLobbyPreviewProvider
import com.quizzgame.feature.room.mobile.ui.tags.MobileLobbyTags
import com.quizzgame.shared.room.domain.entity.command.RoomCommand
import com.quizzgame.shared.room.domain.entity.session.RoomStreamState
import com.quizzgame.shared.room.domain.entity.snapshot.RoomPhase
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MobileLobbyUiTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun readyUsesTheCurrentPlayerAndBackRequiresConfirmation() {
        val state = MobileLobbyPreviewProvider().values.first { it.stream is RoomStreamState.Active }
        val sent = mutableListOf<RoomCommand>()
        val screen = MobileLobbyScreenObject(compose)
        val label = InstrumentationStrings(compose.activity).leave
        compose.setContent { QuizzMobilePreviewScreen { MobileLobbyContent(state, {}, sent::add, {}) } }
        screen.ready.performClick()
        pressBack()
        screen.confirmation(label).assertIsDisplayed()
        compose.runOnIdle { assertEquals(listOf(RoomCommand.Ready(false)), sent) }
    }

    @Test
    fun startedScreenHasNoActionAndSystemBackCanLeave() {
        val state = MobileLobbyPreviewProvider().values.first { it.snapshot?.phase == RoomPhase.IN_GAME }
        val screen = MobileLobbyScreenObject(compose)
        compose.setContent { QuizzMobilePreviewScreen { MobileLobbyContent(state, {}, {}, {}) } }

        screen.exit.assertDoesNotExist()
        pressBack()
        screen.confirmation(InstrumentationStrings(compose.activity).leave).assertIsDisplayed()
    }

    @Test
    fun unavailableRoomHasAReturnToPlayAction() {
        val state = MobileLobbyPreviewProvider().values.first { it.stream is RoomStreamState.Failed }
        val screen = MobileLobbyScreenObject(compose)
        var returned = false
        compose.setContent { QuizzMobilePreviewScreen { MobileLobbyContent(state, { returned = true }, {}, {}) } }

        screen.exit.performClick()
        compose.runOnIdle { assertEquals(true, returned) }
    }

    @Test
    fun showsTheGameDetailsFromTheCatalog() {
        val state = MobileLobbyPreviewProvider().values.first { it.stream is RoomStreamState.Active }
        val screen = MobileLobbyScreenObject(compose)
        val description = requireNotNull(state.gameInfo?.description)
        val questions = compose.activity.resources.getQuantityString(R.plurals.room_game_questions, 12, 12)
        compose.setContent { QuizzMobilePreviewScreen { MobileLobbyContent(state, {}, {}, {}) } }

        screen.text(description).assertIsDisplayed()
        screen.text(questions).assertIsDisplayed()
    }

    private class InstrumentationStrings(activity: ComponentActivity) {
        val leave: String = activity.getString(R.string.room_leave_title)
    }
}

private class MobileLobbyScreenObject(private val provider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<MobileLobbyScreenObject>(provider) {
    val ready = KNode(provider) { hasTestTag(MobileLobbyTags.READY) }
    val exit = KNode(provider) { hasTestTag(MobileLobbyTags.EXIT) }
    fun confirmation(title: String) = text(title)
    fun text(value: String) = KNode(provider) { hasText(value) }
}
