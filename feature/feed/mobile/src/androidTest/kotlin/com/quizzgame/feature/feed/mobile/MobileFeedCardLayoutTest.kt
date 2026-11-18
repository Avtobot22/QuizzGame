package com.quizzgame.feature.feed.mobile

import androidx.activity.ComponentActivity
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzMobileLayout
import com.quizzgame.core.designsystem.mobile.QuizzMobileTheme
import com.quizzgame.feature.feed.mobile.ui.card.MobileFeedHero
import com.quizzgame.feature.feed.mobile.ui.card.MobileQuizCard
import com.quizzgame.feature.feed.mobile.ui.preview.MobileFeedPreviewFixtures
import com.quizzgame.feature.feed.mobile.ui.status.MobileRailSkeleton
import com.quizzgame.feature.feed.mobile.ui.tags.MobileFeedTags
import com.quizzgame.shared.catalog.domain.entity.QuizCard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MobileFeedCardLayoutTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun badgeBookmarkProgressAndMissingSubtitleKeepCardsAlignedAtLargeFontScale() {
        val cards = MobileFeedPreviewFixtures.rail.cards.mapIndexed { index, card ->
            if (index == 0) card.copy(subtitle = null) else card
        }
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, 1.6f)) {
                QuizzMobileTheme {
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        cards.forEach { card ->
                            MobileQuizCard(card, {}, Modifier.width(QuizzMobileLayout.railCardWidth))
                        }
                        MobileRailSkeleton(Modifier.width(QuizzMobileLayout.railCardWidth).testTag(SKELETON))
                    }
                }
            }
        }
        val screen = MobileFeedScreen(compose)
        screen.card(cards.first().id).assertIsDisplayed()
        val heights = cards.map { screen.bounds(MobileFeedTags.card(it.id)).height }
        heights.forEach { assertEquals(heights.first(), it, 1f) }
        assertEquals(heights.first(), screen.bounds(SKELETON).height, 1f)
        val first = cards.first()
        assertLabelBetweenArtworkAndTitle(screen, first, checkNotNull(first.badge).text)
    }

    @Test
    fun heroPlacesBothLabelsBetweenArtworkAndTitle() {
        val card = MobileFeedPreviewFixtures.rail.cards.first()
        compose.setContent {
            QuizzMobileTheme { MobileFeedHero(card, {}, Modifier.widthIn(max = 340.dp)) }
        }
        val screen = MobileFeedScreen(compose)
        assertLabelBetweenArtworkAndTitle(screen, card, checkNotNull(card.badge).text)
        assertLabelBetweenArtworkAndTitle(screen, card, compose.activity.getString(R.string.feed_tv_label))
    }

    private fun assertLabelBetweenArtworkAndTitle(screen: MobileFeedScreen, card: QuizCard, label: String) {
        val artwork = screen.bounds(MobileFeedTags.artwork(card.id))
        val badge = screen.cardTextBounds(card.id, label)
        val title = screen.cardTextBounds(card.id, card.title)
        assertTrue(artwork.bottom <= badge.top)
        assertTrue(badge.bottom <= title.top)
    }
    companion object { private const val SKELETON = "card-layout-skeleton" }
}
