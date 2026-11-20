package com.quizzgame.feature.quizentry.mobile

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import com.quizzgame.core.designsystem.mobile.QuizzMobileTheme
import com.quizzgame.feature.quizentry.mobile.presentation.MobileQuizEntryState
import com.quizzgame.feature.quizentry.mobile.ui.MobileQuizEntryStateContent
import com.quizzgame.feature.quizentry.mobile.ui.preview.MobileQuizEntryPreviewProvider
import com.quizzgame.feature.quizentry.mobile.ui.tags.MobileQuizEntryTags
import com.quizzgame.shared.catalog.domain.entity.PassingStatus
import com.quizzgame.shared.catalog.domain.entity.QuizUserState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import com.quizzgame.core.error.ui.common.R as ErrorR

@RunWith(Parameterized::class)
class MobileQuizEntryVisualTest(private val index: Int, private val dark: Boolean, private val fontScale: Float) {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun skeletonAndDetailsKeepNavigationAndConnectionActionUsable() {
        val state = MobileQuizEntryPreviewProvider().values.elementAt(index)
        var wentBack = false
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(3f, fontScale)) {
                QuizzMobileTheme(darkTheme = dark) {
                    Box(Modifier.fillMaxSize().testTag(MobileQuizEntryVisualScreen.ROOT)) {
                        MobileQuizEntryStateContent(state, {}, { wentBack = true }, {})
                    }
                }
            }
        }
        val screen = MobileQuizEntryVisualScreen(compose)
        if (state == MobileQuizEntryState.Loading) {
            screen.tag(MobileQuizEntryTags.LOADING).assertIsDisplayed()
        } else if (state is MobileQuizEntryState.Content) {
            screen.capture("mobile-entry-$index-$dark-$fontScale-top.png")
            assertUserState(screen, state.value.user)
            screen.tag(MobileQuizEntryTags.CONNECT).performScrollTo()
            screen.tag(MobileQuizEntryTags.CONNECT).assertIsDisplayed()
            if (state.value.available) {
                screen.tag(MobileQuizEntryTags.CONNECT).assertIsEnabled()
            } else {
                screen.tag(MobileQuizEntryTags.CONNECT).assertIsNotEnabled()
                assertEquals(
                    screen.bounds(MobileQuizEntryTags.CONNECT).center.x,
                    screen.bounds(MobileQuizEntryTags.UNAVAILABLE).center.x,
                    1f,
                )
            }
        }
        screen.capture("mobile-entry-$index-$dark-$fontScale.png")
        if (state is MobileQuizEntryState.Error) {
            val back = compose.activity.getString(ErrorR.string.quizz_error_action_go_back)
            screen.text(back).performClick()
            compose.runOnIdle { assertEquals(true, wentBack) }
        }
    }

    private fun assertUserState(screen: MobileQuizEntryVisualScreen, user: QuizUserState) {
        val favorite = screen.tag(MobileQuizEntryTags.FAVORITE)
        val progress = screen.tag(MobileQuizEntryTags.PROGRESS)
        val completed = screen.tag(MobileQuizEntryTags.COMPLETED)
        if (user.bookmarked) {
            favorite.performScrollTo()
            favorite.assertIsDisplayed()
        } else {
            favorite.assertDoesNotExist()
        }
        if (user.status == PassingStatus.IN_PROGRESS) {
            progress.performScrollTo()
            progress.assertIsDisplayed()
            assertEquals(user.progress.toFloat(), screen.progress(), 0.001f)
        } else {
            progress.assertDoesNotExist()
        }
        if (user.status == PassingStatus.COMPLETED) {
            completed.performScrollTo()
            completed.assertIsDisplayed()
        } else {
            completed.assertDoesNotExist()
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "state {0}, dark {1}, font {2}")
        fun states(): List<Array<Any>> = listOf(0, 1, 2, 3, 5, 6, 7).flatMap { index ->
            listOf(arrayOf<Any>(index, false, 1f), arrayOf<Any>(index, true, 1f))
        } + listOf(3, 5, 6).map { arrayOf<Any>(it, false, 1.6f) }
    }
}
