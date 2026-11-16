package com.quizzgame.feature.feed.tv.presentation

import androidx.lifecycle.ViewModelStore
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import androidx.paging.testing.asSnapshot
import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.flags.AppFeatureFlags
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.core.session.lifecycle.SessionStateReader
import com.quizzgame.feature.feed.tv.FakeFeedRemoteDataSource
import com.quizzgame.feature.feed.tv.FeedTestData
import com.quizzgame.feature.feed.tv.data.FeedRepositoryImpl
import com.quizzgame.feature.feed.tv.domain.LoadFeedUseCase
import com.quizzgame.feature.feed.tv.domain.LoadRailItemsUseCase
import com.quizzgame.feature.feed.tv.domain.entity.FeedResult
import com.quizzgame.feature.feed.tv.domain.entity.QuizRail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TvFeedViewModelTest {
    private val flags = object : AppFeatureFlags { override fun guestAllowed(): Boolean = true }

    @Test
    fun pagingIsMandatoryAndHorizontalPagersAreCreatedOnlyOnDemand() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        try {
            val remote = FakeFeedRemoteDataSource()
            val repository = FeedRepositoryImpl(remote, SessionStateReader { SessionState.Guest })
            val model = TvFeedViewModel(
                LoadFeedUseCase(repository),
                LoadRailItemsUseCase(repository),
                flags,
            )
            store.put("feed", model)
            var generations = 0
            backgroundScope.launch { model.state.feedSections().collect { generations++ } }
            val sections = model.state.feedSections().asSnapshot { scrollTo(2) }
            assertEquals(3, sections.size)
            assertTrue(remote.itemCalls.isEmpty())
            val rail = sections.first()
            val flow = model.items(rail)
            val cards = flow.asSnapshot { scrollTo(2) }
            assertEquals(3, cards.size)
            assertEquals(cards, model.items(rail).asSnapshot { scrollTo(2) })
            assertEquals(1, remote.itemCalls.size)
            val beforeNavigation = generations
            model.onIntent(TvFeedIntent.Open(cards.last().entryId))
            model.onIntent(TvFeedIntent.Open(cards.first().id))
            assertEquals(cards.last().entryId, (model.state.value as TvFeedState.Content).destination)
            model.onIntent(TvFeedIntent.NavigationHandled)
            assertEquals(null, (model.state.value as TvFeedState.Content).destination)
            runCurrent()
            assertEquals(beforeNavigation, generations)
        } finally {
            store.clear()
            runCurrent()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun clearingViewModelCancelsOutstandingPaginationRequests() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        try {
            val remote = FakeFeedRemoteDataSource()
            val repository = FeedRepositoryImpl(remote, SessionStateReader { SessionState.Guest })
            var cancelled = false
            remote.feedReply = {
                try { awaitCancellation() } finally { cancelled = true }
            }
            val model = TvFeedViewModel(
                LoadFeedUseCase(repository),
                LoadRailItemsUseCase(repository),
                flags,
            )
            store.put("feed", model)
            val presenter = object : PagingDataPresenter<QuizRail>() {
                override suspend fun presentPagingDataEvent(event: PagingDataEvent<QuizRail>) = Unit
            }
            backgroundScope.launch { model.state.feedSections().collectLatest(presenter::collectFrom) }
            runCurrent()
            store.clear()
            runCurrent()
            assertTrue(cancelled)
            assertTrue(presenter.snapshot().items.isEmpty())
        } finally {
            store.clear()
            runCurrent()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun refreshFailureKeepsLoadedSectionsAndRetryUsesSameRequest() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        try {
            val remote = FakeFeedRemoteDataSource()
            val repository = FeedRepositoryImpl(remote, SessionStateReader { SessionState.Guest })
            val model = TvFeedViewModel(
                LoadFeedUseCase(repository),
                LoadRailItemsUseCase(repository),
                flags,
            )
            store.put("feed", model)
            val presenter = object : PagingDataPresenter<QuizRail>() {
                override suspend fun presentPagingDataEvent(event: PagingDataEvent<QuizRail>) = Unit
            }
            backgroundScope.launch { model.state.feedSections().collectLatest(presenter::collectFrom) }
            runCurrent()
            val before = presenter.snapshot().items
            assertTrue(before.isNotEmpty())
            remote.feedReply = { FeedResult.Failed(AppFailure.InvalidServerData) }
            presenter.refresh()
            runCurrent()
            assertEquals(before, presenter.snapshot().items)
            remote.feedReply = { FeedResult.Loaded(FeedTestData.feed("feed-page-2.json")) }
            presenter.retry()
            runCurrent()
            assertEquals(1, presenter.snapshot().items.size)
        } finally {
            store.clear()
            runCurrent()
            Dispatchers.resetMain()
        }
    }
}
