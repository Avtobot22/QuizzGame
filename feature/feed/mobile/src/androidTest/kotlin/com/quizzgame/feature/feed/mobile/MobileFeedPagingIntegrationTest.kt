package com.quizzgame.feature.feed.mobile

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.Density
import androidx.lifecycle.ViewModelStore
import androidx.paging.compose.collectAsLazyPagingItems
import com.quizzgame.core.designsystem.mobile.preview.QuizzMobilePreviewScreen
import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.error.failure.ConnectivityKind
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.feature.feed.mobile.presentation.MobileFeedViewModel
import com.quizzgame.feature.feed.mobile.ui.MobileFeedStateContent
import com.quizzgame.feature.feed.mobile.ui.paging.MobileFeedPagingEffects
import com.quizzgame.feature.feed.mobile.ui.preview.MobileFeedPreviewFixtures
import com.quizzgame.feature.feed.mobile.ui.tags.MobileFeedTags
import com.quizzgame.shared.catalog.domain.entity.CursorPageInfo
import com.quizzgame.shared.feed.data.FeedRemoteDataSource
import com.quizzgame.shared.feed.data.FeedRepositoryImpl
import com.quizzgame.shared.feed.domain.LoadFeedUseCase
import com.quizzgame.shared.feed.domain.LoadRailItemsUseCase
import com.quizzgame.shared.feed.domain.entity.Feed
import com.quizzgame.shared.feed.domain.entity.FeedResult
import com.quizzgame.shared.feed.domain.entity.RailItems
import kotlinx.coroutines.CompletableDeferred
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger
import com.quizzgame.core.error.ui.common.R as ErrorR

class MobileFeedPagingIntegrationTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()
    private val store = ViewModelStore()
    private val screen get() = MobileFeedScreen(compose)
    private val seed get() = MobileFeedPreviewFixtures.rail.copy(cards = MobileFeedPreviewFixtures.rail.cards.take(1))
    private val retryLabel get() = compose.activity.getString(ErrorR.string.quizz_error_action_retry)

    @After
    fun clearModels() { compose.runOnUiThread { store.clear() } }

    @Test
    fun initialLoadBecomesEmptyWithoutAStuckSpinner() {
        val gate = CompletableDeferred<Feed>()
        val remote = object : FakeFeedRemote() {
            override suspend fun load(cursor: String?, owner: SessionState?) = FeedResult.Loaded(gate.await())
        }
        showFeed(remote)
        screen.tag(MobileFeedTags.LOADING).assertIsDisplayed()
        compose.runOnIdle { gate.complete(Feed(emptyList())) }
        screen.tag(MobileFeedTags.EMPTY).assertIsDisplayed()
        screen.tag(MobileFeedTags.LOADING).assertDoesNotExist()
    }

    @Test
    fun initialFailureCanRetryAndShowContent() {
        val attempts = AtomicInteger()
        val remote = object : FakeFeedRemote() {
            override suspend fun load(cursor: String?, owner: SessionState?) =
                if (attempts.incrementAndGet() == 1) failure() else FeedResult.Loaded(Feed(listOf(seed)))
        }
        showFeed(remote)
        screen.tag(MobileFeedTags.ERROR).assertIsDisplayed()
        screen.text(retryLabel).performClick()
        screen.card(seed.cards.first().id).assertIsDisplayed()
        screen.tag(MobileFeedTags.ERROR).assertDoesNotExist()
    }

    @Test
    fun delayedRailAppendKeepsInlineCardAndClickUsesAppendedEntryId() {
        val rail = seed.copy(itemsPage = CursorPageInfo(true, "after-inline"))
        val card = rail.cards.first().copy(id = "appended-snippet", entryId = "appended-entry")
        val gate = CompletableDeferred<RailItems>()
        val remote = object : FakeFeedRemote() {
            override suspend fun load(cursor: String?, owner: SessionState?) = FeedResult.Loaded(Feed(listOf(rail)))
            override suspend fun loadItems(sectionId: String, cursor: String, owner: SessionState) =
                FeedResult.Loaded(gate.await())
        }
        val model = showFeed(remote)
        screen.card(rail.cards.first().id).assertIsDisplayed()
        screen.tag(MobileFeedTags.appendingRail(rail.id)).assertExists()
        compose.runOnIdle { gate.complete(RailItems(rail.id, listOf(card), CursorPageInfo(false, null))) }
        screen.tag(MobileFeedTags.row(rail.id)).performTouchInput { swipeLeft() }
        screen.card(card.id).performClick()
        compose.runOnIdle { assertEquals(card.entryId, model.state.value.destination) }
    }

    @Test
    fun sectionAppendKeepsVisibleCardsAndAddsTheNextRail() {
        val gate = CompletableDeferred<Feed>()
        val next = seed.copy(id = "next-rail", cards = listOf(seed.cards.first().copy(id = "next-card")))
        val remote = object : FakeFeedRemote() {
            override suspend fun load(cursor: String?, owner: SessionState?) = FeedResult.Loaded(
                if (cursor == null) Feed(listOf(seed), CursorPageInfo(true, "next-section")) else gate.await(),
            )
        }
        showFeed(remote)
        screen.card(seed.cards.first().id).assertIsDisplayed()
        screen.tag(MobileFeedTags.APPENDING_SECTIONS).assertExists()
        compose.runOnIdle { gate.complete(Feed(listOf(next))) }
        screen.card("next-card").performScrollTo()
        screen.card("next-card").assertIsDisplayed()
    }

    @Test
    fun failedSectionAppendRetriesContinuationWithoutReloadingTheFirstPage() {
        val initial = AtomicInteger()
        val appends = AtomicInteger()
        val next = seed.copy(id = "retried-section", cards = listOf(seed.cards.first().copy(id = "section-card")))
        val remote = object : FakeFeedRemote() {
            override suspend fun load(cursor: String?, owner: SessionState?): FeedResult<Feed> = when {
                cursor == null -> {
                    initial.incrementAndGet()
                    FeedResult.Loaded(Feed(listOf(seed), CursorPageInfo(true, "next-section")))
                }
                appends.incrementAndGet() == 1 -> failure()
                else -> FeedResult.Loaded(Feed(listOf(next)))
            }
        }
        showFeed(remote)
        screen.card(seed.cards.first().id).assertIsDisplayed()
        screen.tag(MobileFeedTags.APPEND_ERROR).performScrollTo()
        screen.text(retryLabel).performClick()
        screen.card("section-card").performScrollTo()
        screen.card("section-card").assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(1, initial.get())
            assertEquals(2, appends.get())
        }
    }

    @Test
    fun railAppendFailureRetriesOnlyThatRailAndRetainsInlineContent() {
        val rail = seed.copy(itemsPage = CursorPageInfo(true, "after-inline"))
        val next = rail.cards.first().copy(id = "retried-card")
        val attempts = AtomicInteger()
        val remote = object : FakeFeedRemote() {
            override suspend fun load(cursor: String?, owner: SessionState?) = FeedResult.Loaded(Feed(listOf(rail)))
            override suspend fun loadItems(
                sectionId: String,
                cursor: String,
                owner: SessionState,
            ): FeedResult<RailItems> =
                if (attempts.incrementAndGet() == 1) {
                    failure()
                } else {
                    FeedResult.Loaded(RailItems(sectionId, listOf(next), CursorPageInfo(false, null)))
                }
        }
        showFeed(remote, fontScale = 1.6f)
        val cardId = rail.cards.first().id
        screen.card(cardId).assertIsDisplayed()
        val cardSize = screen.unclippedSize(MobileFeedTags.card(cardId))
        val retryTag = MobileFeedTags.retryRail(rail.id)
        screen.tag(retryTag).performScrollTo()
        val errorSize = screen.unclippedSize(retryTag)
        assertEquals(cardSize.width.value, errorSize.width.value, 0.01f)
        assertEquals(cardSize.height.value, errorSize.height.value, 0.01f)
        screen.text(retryLabel).assertIsDisplayed()
        screen.text(retryLabel).performClick()
        screen.tag(MobileFeedTags.row(rail.id)).performTouchInput { swipeLeft() }
        screen.card(next.id).assertIsDisplayed()
        compose.runOnIdle { assertEquals(2, attempts.get()) }
    }

    @Test
    fun failedPullToRefreshKeepsContentAndBannerRetryRecovers() {
        val attempts = AtomicInteger()
        val remote = object : FakeFeedRemote() {
            override suspend fun load(cursor: String?, owner: SessionState?) =
                if (attempts.incrementAndGet() == 2) failure() else FeedResult.Loaded(Feed(listOf(seed)))
        }
        showFeed(remote)
        screen.card(seed.cards.first().id).assertIsDisplayed()
        screen.tag(MobileFeedTags.REFRESH).performTouchInput { swipeDown() }
        screen.tag(MobileFeedTags.REFRESH_ERROR).assertIsDisplayed()
        screen.card(seed.cards.first().id).assertIsDisplayed()
        screen.text(retryLabel).performClick()
        screen.tag(MobileFeedTags.REFRESH_ERROR).assertDoesNotExist()
        screen.card(seed.cards.first().id).assertIsDisplayed()
        compose.runOnIdle { assertEquals(3, attempts.get()) }
    }

    private fun showFeed(remote: FeedRemoteDataSource, fontScale: Float? = null): MobileFeedViewModel {
        val repository = FeedRepositoryImpl(remote) { SessionState.Guest }
        lateinit var model: MobileFeedViewModel
        compose.runOnUiThread {
            model = MobileFeedViewModel(LoadFeedUseCase(repository), LoadRailItemsUseCase(repository))
            store.put("feed", model)
        }
        compose.setContent {
            val sections = model.feed.collectAsLazyPagingItems()
            MobileFeedPagingEffects(model.events, sections)
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale ?: density.fontScale)) {
                QuizzMobilePreviewScreen { MobileFeedStateContent(sections, model::items, model::onIntent, {}) }
            }
        }
        return model
    }

    private open inner class FakeFeedRemote : FeedRemoteDataSource {
        override suspend fun load(cursor: String?, owner: SessionState?): FeedResult<Feed> =
            FeedResult.Loaded(Feed(listOf(seed)))
        override suspend fun loadItems(sectionId: String, cursor: String, owner: SessionState): FeedResult<RailItems> =
            error("Inline-only rail must not make an items request")
    }
    private fun failure() = FeedResult.Failed(AppFailure.Connectivity(ConnectivityKind.TIMEOUT))
}
