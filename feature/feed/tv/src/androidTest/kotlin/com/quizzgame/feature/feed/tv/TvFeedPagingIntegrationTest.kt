package com.quizzgame.feature.feed.tv

import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.pressKey
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import com.quizzgame.core.designsystem.tv.preview.QuizzTvPreviewScreen
import com.quizzgame.core.flags.AppFeatureFlags
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.core.session.lifecycle.SessionStateReader
import com.quizzgame.feature.feed.tv.data.FeedRemoteDataSource
import com.quizzgame.feature.feed.tv.data.FeedRepositoryImpl
import com.quizzgame.feature.feed.tv.domain.LoadFeedUseCase
import com.quizzgame.feature.feed.tv.domain.LoadRailItemsUseCase
import com.quizzgame.feature.feed.tv.domain.entity.CursorPageInfo
import com.quizzgame.feature.feed.tv.domain.entity.Feed
import com.quizzgame.feature.feed.tv.domain.entity.FeedResult
import com.quizzgame.feature.feed.tv.domain.entity.RailItems
import com.quizzgame.feature.feed.tv.presentation.TvFeedState
import com.quizzgame.feature.feed.tv.presentation.TvFeedViewModel
import com.quizzgame.feature.feed.tv.presentation.feedSections
import com.quizzgame.feature.feed.tv.ui.TvPagedFeedContent
import com.quizzgame.feature.feed.tv.ui.preview.TvFeedPreviewProvider
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TvFeedPagingIntegrationTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()
    private val store = ViewModelStore()

    @After
    fun clearModels() { compose.runOnUiThread { store.clear() } }

    @Test
    fun delayedAppendKeepsFocusAndAppendedCardOpensItsEntryId() {
        val preview = TvFeedPreviewProvider().values.filterIsInstance<TvFeedState.Content>().first().value
        val seed = preview.rails.first().copy(itemsPage = CursorPageInfo(true, "after-preview"))
        val appended = seed.cards.first().copy(id = "appended-snippet", entryId = "appended-entry")
        val gate = CompletableDeferred<RailItems>()
        val repository = object : FeedRemoteDataSource {
            override suspend fun load(cursor: String?, owner: SessionState?) = FeedResult.Loaded(Feed(listOf(seed)))
            override suspend fun loadItems(sectionId: String, cursor: String, owner: SessionState) =
                FeedResult.Loaded(gate.await())
        }
        val model = showFeed(repository)
        val screen = FeedPagingScreen(compose)
        val first = screen.card("quiz-0")
        first.assertIsFocused()
        compose.runOnIdle { gate.complete(RailItems(seed.id, listOf(appended), CursorPageInfo(false, null))) }
        first.assertIsFocused()
        repeat(seed.cards.size) { onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_DPAD_RIGHT)) }
        screen.card(appended.id).assertIsFocused()
        onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_DPAD_CENTER))
        compose.runOnIdle { assertEquals(appended.entryId, (model.state.value as TvFeedState.Content).destination) }
    }

    @Test
    fun failedRailAppendKeepsCardsAndCanRetryWithDpad() {
        val seed = TvFeedPreviewProvider().values.filterIsInstance<TvFeedState.Content>().first().value.rails.first()
            .copy(itemsPage = CursorPageInfo(true, "after-preview"))
        val appended = seed.cards.first().copy(id = "retried-snippet", entryId = "retried-entry")
        var calls = 0
        val repository = object : FeedRemoteDataSource {
            override suspend fun load(cursor: String?, owner: SessionState?) = FeedResult.Loaded(Feed(listOf(seed)))
            override suspend fun loadItems(
                sectionId: String,
                cursor: String,
                owner: SessionState,
            ): FeedResult<RailItems> {
                calls++
                return if (calls == 1) {
                    FeedResult.Failed(
                        com.quizzgame.core.error.failure.AppFailure.Connectivity(
                            com.quizzgame.core.error.failure.ConnectivityKind.TIMEOUT,
                        ),
                    )
                } else {
                    FeedResult.Loaded(RailItems(seed.id, listOf(appended), CursorPageInfo(false, null)))
                }
            }
        }
        showFeed(repository)
        val screen = FeedPagingScreen(compose)
        screen.card(seed.cards.first().id).assertIsFocused()
        screen.retry(seed.id).assertExists()
        repeat(seed.cards.size) { onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_DPAD_RIGHT)) }
        onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_DPAD_CENTER))
        screen.card(appended.id).assertIsDisplayed()
        compose.runOnIdle { assertEquals(2, calls) }
    }

    private fun showFeed(remote: FeedRemoteDataSource): TvFeedViewModel {
        val repository = FeedRepositoryImpl(remote, SessionStateReader { SessionState.Guest })
        lateinit var model: TvFeedViewModel
        compose.runOnUiThread {
            model = TvFeedViewModel(
                LoadFeedUseCase(repository),
                LoadRailItemsUseCase(repository),
                object : AppFeatureFlags { override fun guestAllowed(): Boolean = true },
            )
            store.put("feed", model)
        }
        compose.setContent {
            val state by model.state.collectAsStateWithLifecycle()
            val scope = rememberCoroutineScope()
            val pages = remember(model, scope) {
                model.state.feedSections().stateIn(
                    scope,
                    SharingStarted.Eagerly,
                    (model.state.value as? TvFeedState.Content)?.sections ?: PagingData.empty(),
                )
            }
            val sections = pages.collectAsLazyPagingItems()
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                QuizzTvPreviewScreen(Modifier.size(960.dp, 540.dp)) {
                    if (sections.itemCount > 0) {
                        TvPagedFeedContent(sections, model, state as TvFeedState.Content, true, Modifier, {})
                    }
                }
            }
        }
        return model
    }
}
