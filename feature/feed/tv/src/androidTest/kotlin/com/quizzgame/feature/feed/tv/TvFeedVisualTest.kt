package com.quizzgame.feature.feed.tv

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.quizzgame.core.designsystem.tv.preview.QuizzTvPreviewScreen
import com.quizzgame.feature.feed.tv.ui.preview.TvFeedPreviewProvider
import com.quizzgame.feature.feed.tv.ui.preview.TvFeedPreviewState
import com.quizzgame.feature.feed.tv.ui.preview.TvFeedPreviewStateContent
import com.quizzgame.feature.feed.tv.ui.tags.TvFeedTags
import com.quizzgame.feature.feed.tv.ui.tags.TvVisualTags
import com.quizzgame.shared.feed.domain.entity.section.QuizRail
import io.github.kakaocup.compose.node.element.KNode
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class TvFeedVisualTest(private val index: Int) {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun stateFitsTheTvViewport() {
        val state = TvFeedPreviewProvider().values.elementAt(index)
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                QuizzTvPreviewScreen(Modifier.size(960.dp, 540.dp).testTag(TvVisualTags.ROOT)) {
                    TvFeedPreviewStateContent(state, {}, false, Modifier, {})
                }
            }
        }
        compose.waitForIdle()
        val stateTag = when (state) {
            TvFeedPreviewState.Loading -> TvFeedTags.LOADING
            TvFeedPreviewState.Empty -> TvFeedTags.EMPTY
            is TvFeedPreviewState.Content -> TvFeedTags.CONTENT
            is TvFeedPreviewState.Error -> TvVisualTags.ROOT
        }
        KNode(compose) { hasTestTag(stateTag) }.assertIsDisplayed()
        if (state is TvFeedPreviewState.Content) assertBadgeBetweenArtworkAndTitle(state)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = checkNotNull(context.getExternalFilesDir("visual-previews"))
        File(directory, "feed-$index.png").outputStream().use { output ->
            val bitmap = compose.onNodeWithTag(TvVisualTags.ROOT).captureToImage().asAndroidBitmap()
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
        }
    }

    private fun assertBadgeBetweenArtworkAndTitle(state: TvFeedPreviewState.Content) {
        val card = state.value.sections.filterIsInstance<QuizRail>().first().cards.first()
        val screen = FeedPagingScreen(compose)
        val artwork = screen.artworkBounds(card.id)
        val badge = screen.cardTextBounds(card.id, checkNotNull(card.badge).text)
        val title = screen.cardTextBounds(card.id, card.title)
        assertTrue(artwork.bottom <= badge.top)
        assertTrue(badge.bottom <= title.top)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "state {0}")
        fun states(): List<Array<Any>> = TvFeedPreviewProvider().values.mapIndexed { index, _ ->
            arrayOf<Any>(index)
        }.toList()
    }
}
