package com.quizzgame.feature.feed.mobile

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import androidx.paging.LoadState
import androidx.test.platform.app.InstrumentationRegistry
import com.quizzgame.core.designsystem.mobile.QuizzMobileTheme
import com.quizzgame.feature.feed.mobile.ui.preview.MobileFeedPreviewFixtures
import com.quizzgame.feature.feed.mobile.ui.preview.MobileFeedPreviewProvider
import com.quizzgame.feature.feed.mobile.ui.preview.MobileFeedPreviewState
import com.quizzgame.feature.feed.mobile.ui.preview.MobileFeedPreviewStateContent
import com.quizzgame.feature.feed.mobile.ui.tags.MobileFeedTags
import com.quizzgame.shared.feed.domain.entity.QuizHero
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import java.util.Locale

@RunWith(Parameterized::class)
class MobileFeedVisualTest(private val index: Int, private val dark: Boolean) {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun providerStateRendersInMobileThemeWithoutHiltOrNetwork() {
        val state = MobileFeedPreviewProvider().values.elementAt(index)
        compose.setContent {
            val original = LocalContext.current
            val currentConfiguration = LocalConfiguration.current
            val configuration = remember(currentConfiguration) {
                Configuration(currentConfiguration).apply { setLocale(Locale.forLanguageTag("ru")) }
            }
            val localized = remember(original, configuration) { original.createConfigurationContext(configuration) }
            CompositionLocalProvider(
                LocalDensity provides Density(3f, 1f),
                LocalContext provides localized,
                LocalConfiguration provides configuration,
            ) {
                QuizzMobileTheme(darkTheme = dark) {
                    Box(Modifier.fillMaxSize().testTag(ROOT)) { MobileFeedPreviewStateContent(state) }
                }
            }
        }
        val tag = when (state) {
            MobileFeedPreviewState.Loading -> MobileFeedTags.LOADING
            MobileFeedPreviewState.Empty -> MobileFeedTags.EMPTY
            is MobileFeedPreviewState.Error -> MobileFeedTags.ERROR
            is MobileFeedPreviewState.Content -> contentTag(state)
        }
        val screen = MobileFeedScreen(compose)
        val target = screen.tag(tag)
        if (state is MobileFeedPreviewState.Content && tag != MobileFeedTags.LIST) target.performScrollTo()
        target.assertIsDisplayed()
        if (state is MobileFeedPreviewState.Content && tag == MobileFeedTags.LIST) {
            state.value.sections.filterIsInstance<QuizHero>().singleOrNull()?.let { section ->
                val heading = MobileFeedTags.FEATURED_HEADING
                val card = MobileFeedTags.card(section.card.id)
                screen.tag(heading).assertIsDisplayed()
                assertTrue(screen.bounds(heading).bottom < screen.bounds(card).top)
            }
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = checkNotNull(context.getExternalFilesDir("visual-previews"))
        File(directory, "mobile-feed-$index-$dark.png").outputStream().use { output ->
            val bitmap = compose.onNodeWithTag(ROOT).captureToImage().asAndroidBitmap()
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
        }
    }
    private fun contentTag(state: MobileFeedPreviewState.Content): String = when {
        state.append is LoadState.Loading -> MobileFeedTags.APPENDING_SECTIONS
        state.append is LoadState.Error -> MobileFeedTags.APPEND_ERROR
        state.railAppend is LoadState.Loading -> MobileFeedTags.appendingRail(MobileFeedPreviewFixtures.rail.id)
        state.railAppend is LoadState.Error -> MobileFeedTags.retryRail(MobileFeedPreviewFixtures.rail.id)
        state.refresh is LoadState.Error -> MobileFeedTags.REFRESH_ERROR
        else -> MobileFeedTags.LIST
    }

    companion object {
        private const val ROOT = "mobile-feed-visual-root"

        @JvmStatic
        @Parameterized.Parameters(name = "state {0}, dark {1}")
        fun states(): List<Array<Any>> = MobileFeedPreviewProvider().values.flatMapIndexed { index, _ ->
            sequenceOf(arrayOf<Any>(index, false), arrayOf<Any>(index, true))
        }.toList()
    }
}
