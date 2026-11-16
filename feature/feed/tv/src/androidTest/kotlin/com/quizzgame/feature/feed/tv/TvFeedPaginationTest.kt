package com.quizzgame.feature.feed.tv

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.tv.preview.QuizzTvPreviewScreen
import com.quizzgame.feature.feed.tv.presentation.TvFeedIntent
import com.quizzgame.feature.feed.tv.presentation.TvFeedState
import com.quizzgame.feature.feed.tv.ui.content.TvFeedStateContent
import com.quizzgame.feature.feed.tv.ui.preview.TvFeedPreviewProvider
import com.quizzgame.feature.feed.tv.ui.tags.TvFeedTags
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TvFeedPaginationTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun appendingBothAxesPreservesCardFocusAndDoesNotAddRemoteTargets() {
        val content = TvFeedPreviewProvider().values.filterIsInstance<TvFeedState.Content>().first()
        val state = mutableStateOf(
            content.copy(
                value = content.value.copy(rails = content.value.rails.map { it.copy(cards = it.cards.take(1)) }),
            ),
        )
        var opened = 0
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                QuizzTvPreviewScreen(Modifier.size(960.dp, 540.dp)) {
                    TvFeedStateContent(
                        state.value,
                        { if (it is TvFeedIntent.Open) opened++ },
                        true,
                        Modifier,
                        {},
                    )
                }
            }
        }
        val screen = FeedPaginationScreen(compose)
        screen.firstCard.assertIsFocused()
        compose.runOnIdle {
            state.value = state.value.copy(appendingSections = true, appendingRails = setOf("popular"))
        }
        screen.firstCard.assertIsFocused()
        screen.assertDecorativeLoading(TvFeedTags.APPENDING_SECTIONS)
        screen.assertDecorativeLoading(TvFeedTags.appendingRail("popular"))
        screen.openCardAndMovePastPlaceholders()
        compose.runOnIdle { assertEquals(1, opened) }
        compose.runOnIdle { state.value = state.value.copy(appendingSections = false, appendingRails = emptySet()) }
        screen.refresh.assertIsFocused()
    }
}
