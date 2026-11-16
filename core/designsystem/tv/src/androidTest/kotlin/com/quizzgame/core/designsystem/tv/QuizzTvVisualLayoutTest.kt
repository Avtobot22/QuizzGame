package com.quizzgame.core.designsystem.tv

import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.tv.media.QuizzTvEmptyState
import com.quizzgame.core.designsystem.tv.preview.QuizzTvPreviewScreen
import com.quizzgame.core.designsystem.tv.ui.tags.TvVisualTags
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test

class QuizzTvVisualLayoutTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()
    private val screen get() = TvVisualScreen(compose)

    @Test
    fun quietDangerActionStaysEnabledAndUsesASeparateFocusedTone() {
        compose.setContent { ActionFixture() }
        screen.action("Sign out").assertIsEnabled()
        val primaryFill = screen.controlFill(TvVisualTags.PRIMARY_ACTION)
        val quietFill = screen.controlFill(TvVisualTags.DANGER_ACTION)
        screen.capture("tv-danger-default.png")
        repeat(2) { TvErrorRemote.press(KeyEvent.KEYCODE_DPAD_RIGHT) }
        screen.action("Sign out").assertIsFocused()
        assertNotEquals(quietFill, screen.controlFill(TvVisualTags.DANGER_ACTION))
        assertNotEquals(primaryFill, screen.controlFill(TvVisualTags.DANGER_ACTION))
        screen.capture("tv-danger-focus.png")
    }

    @Test
    fun primaryAndSecondaryActionsUseDifferentFocusTreatments() {
        compose.setContent { ActionFixture() }
        screen.action("Start").assertIsFocused()
        val primaryFill = screen.controlFill(TvVisualTags.PRIMARY_ACTION)
        screen.capture("tv-primary-focus.png")
        TvErrorRemote.press(KeyEvent.KEYCODE_DPAD_RIGHT)
        screen.action("Back").assertIsFocused()
        assertNotEquals(primaryFill, screen.controlFill(TvVisualTags.PRIMARY_ACTION))
        assertNotEquals(primaryFill, screen.controlFill(TvVisualTags.SECONDARY_ACTION))
        screen.capture("tv-secondary-focus.png")
    }

    @Test
    fun englishNavigationStaysOnOneLineAt720dpAndKeepsFocusSeparateFromSelection() {
        compose.setContent { NavigationFixture() }
        screen.action("Home").assertIsFocused()
        listOf("Home", "Library", "Settings", "Sign out").forEach(screen::assertSingleLine)
        listOf("Home", "Library", "Settings", "Sign out").forEach(screen::assertActionContentCentered)
        screen.assertSeparated(TvVisualTags.tab("Settings"), TvVisualTags.SIGN_OUT)
        TvErrorRemote.press(KeyEvent.KEYCODE_DPAD_RIGHT)
        screen.action("Library").assertIsFocused()
        screen.action("Home").assertIsSelected()
        screen.capture("tv-navigation-en-720.png")
    }

    @Test
    fun emptyStateCentersItsRecoveryActionAndAcceptsRemoteInput() {
        var retries = 0
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                QuizzTvPreviewScreen(Modifier.size(960.dp, 540.dp).testTag(TvVisualTags.ROOT)) {
                    QuizzTvEmptyState(
                        "No quizzes yet",
                        "Try again",
                        { retries++ },
                        description = "Quizzes for your next game night will appear here.",
                    )
                }
            }
        }
        screen.action("Try again").assertIsFocused()
        screen.assertCentered("Try again")
        screen.assertActionContentCentered("Try again")
        screen.capture("tv-empty.png")
        TvErrorRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        compose.runOnIdle { assertEquals(1, retries) }
    }

    @Composable
    private fun ActionFixture() {
        val focus = remember { FocusRequester() }
        CompositionLocalProvider(LocalDensity provides Density(1f)) {
            QuizzTvPreviewScreen(Modifier.size(720.dp, 405.dp).testTag(TvVisualTags.ROOT)) {
                Row(Modifier.padding(32.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    QuizzTvButton({}, Modifier.testTag(TvVisualTags.PRIMARY_ACTION).focusRequester(focus)) {
                        QuizzTvActionLabel("Start")
                    }
                    QuizzTvButton(
                        {},
                        Modifier.testTag(TvVisualTags.SECONDARY_ACTION),
                        style = QuizzTvButtonStyle.SECONDARY,
                    ) { QuizzTvActionLabel("Back") }
                    QuizzTvButton(
                        {},
                        Modifier.testTag(TvVisualTags.DANGER_ACTION),
                        style = QuizzTvButtonStyle.QUIET,
                        tone = QuizzTvButtonTone.DANGER,
                    ) { QuizzTvActionLabel("Sign out") }
                }
            }
        }
        LaunchedEffect(Unit) { focus.requestFocus() }
    }

    @Composable
    private fun NavigationFixture() {
        val focus = remember { FocusRequester() }
        CompositionLocalProvider(LocalDensity provides Density(1f)) {
            QuizzTvPreviewScreen(Modifier.size(720.dp, 405.dp).testTag(TvVisualTags.ROOT)) {
                Box(Modifier.padding(32.dp)) {
                    QuizzTvNavigationBar(
                        listOf("Home", "Library", "Settings"),
                        "Home",
                        {},
                        label = { QuizzTvActionLabel(it) },
                        itemModifier = {
                            Modifier.testTag(TvVisualTags.tab(it))
                                .then(if (it == "Home") Modifier.focusRequester(focus) else Modifier)
                        },
                        trailingContent = {
                            QuizzTvButton(
                                {},
                                Modifier.width(108.dp).testTag(TvVisualTags.SIGN_OUT),
                                style = QuizzTvButtonStyle.QUIET,
                            ) { QuizzTvActionLabel("Sign out") }
                        },
                    )
                }
            }
        }
        LaunchedEffect(Unit) { focus.requestFocus() }
    }
}
