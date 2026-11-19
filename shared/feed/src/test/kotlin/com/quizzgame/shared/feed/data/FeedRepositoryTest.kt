package com.quizzgame.shared.feed.data

import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import androidx.paging.cachedIn
import androidx.paging.testing.asSnapshot
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.core.session.lifecycle.SessionStateReader
import com.quizzgame.shared.feed.FakeFeedRemoteDataSource
import com.quizzgame.shared.feed.FeedTestData
import com.quizzgame.shared.feed.data.paging.FeedPagingSession
import com.quizzgame.shared.feed.domain.entity.FeedResult
import com.quizzgame.shared.feed.domain.entity.section.FeedSection
import com.quizzgame.shared.feed.domain.entity.section.QuizRail
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeedRepositoryTest {
    @Test
    fun repositoryDoesNotCacheRailPagesOrFetchInlinePreviewAgain() = runTest {
        val remote = FakeFeedRemoteDataSource()
        val repository = FeedRepositoryImpl(remote, SessionStateReader { SessionState.Guest })
        val rails = repository.getFeed().cachedIn(backgroundScope).asSnapshot { scrollTo(2) }
        assertTrue(remote.itemCalls.isEmpty())
        val first = repository.loadItems((rails.first() as QuizRail)).asSnapshot { scrollTo(2) }
        val repeated = repository.loadItems((rails.first() as QuizRail)).asSnapshot { scrollTo(2) }
        assertEquals(first, repeated)
        assertEquals(2, remote.itemCalls.size)
    }

    @Test
    fun newViewModelRepositoryDoesNotReusePreviousOwnerPages() = runTest {
        val remote = FakeFeedRemoteDataSource()
        val previous = FeedRepositoryImpl(remote, SessionStateReader { SessionState.Authenticated(1) })
        val old = previous.getFeed().cachedIn(backgroundScope).asSnapshot { scrollTo(2) }
        val repository = FeedRepositoryImpl(remote, SessionStateReader { SessionState.Authenticated(2) })
        val current = repository.getFeed().cachedIn(backgroundScope).asSnapshot { scrollTo(2) }
        assertNotEquals((old.first() as QuizRail).snapshotId, (current.first() as QuizRail).snapshotId)
        assertTrue(repository.loadItems((old.first() as QuizRail)).asSnapshot().isEmpty())
        assertTrue(remote.itemCalls.isEmpty())
        assertEquals(3, repository.loadItems((current.first() as QuizRail)).asSnapshot { scrollTo(2) }.size)
    }

    @Test
    fun explicitRefreshRenewsTheOneAutomaticExpirationRecovery() = runTest {
        val remote = FakeFeedRemoteDataSource()
        remote.feedReply = { cursor ->
            if (cursor == null) FeedResult.Loaded(FeedTestData.feed()) else FeedResult.Expired
        }
        val repository = FeedRepositoryImpl(remote, SessionStateReader { SessionState.Guest })
        val presenter = object : PagingDataPresenter<FeedSection>(StandardTestDispatcher(testScheduler)) {
            override suspend fun presentPagingDataEvent(event: PagingDataEvent<FeedSection>) = Unit
        }
        backgroundScope.launch {
            repository.getFeed().cachedIn(backgroundScope).collectLatest(presenter::collectFrom)
        }
        runCurrent()
        repeat(2) {
            presenter[0]
            runCurrent()
        }
        assertEquals(2, remote.feedCalls.count { it == null })
        assertEquals(2, remote.feedCalls.count { it != null })
        presenter.refresh()
        runCurrent()
        presenter[0]
        runCurrent()
        assertEquals(4, remote.feedCalls.count { it == null })
    }

    @Test
    fun expirationHasOneAutomaticRecoveryUntilAnExplicitRefresh() = runTest {
        val remote = FakeFeedRemoteDataSource()
        remote.feedReply = { cursor ->
            if (cursor == null) FeedResult.Loaded(FeedTestData.feed()) else FeedResult.Expired
        }
        val paging = FeedPagingSession(
            remote,
            SessionState.Guest,
        )
        val failure = runCatching { paging.sections.asSnapshot { scrollTo(2) } }
        assertTrue(failure.isFailure)
        assertEquals(2, remote.feedCalls.count { it == null })
        assertEquals(2, remote.feedCalls.count { it != null })
        paging.close()
    }

    @Test
    fun feedIsColdAndCollectorsHaveIndependentSnapshots() = runTest {
        val remote = FakeFeedRemoteDataSource()
        val repository = FeedRepositoryImpl(remote, SessionStateReader { SessionState.Guest })
        val feed = repository.getFeed()
        assertTrue(remote.feedCalls.isEmpty())
        val first = feed.cachedIn(backgroundScope).asSnapshot { scrollTo(2) }
        val second = feed.cachedIn(backgroundScope).asSnapshot { scrollTo(2) }
        assertNotEquals((first.first() as QuizRail).snapshotId, (second.first() as QuizRail).snapshotId)
        assertEquals(3, repository.loadItems((first.first() as QuizRail)).asSnapshot { scrollTo(2) }.size)
        assertEquals(3, repository.loadItems((second.first() as QuizRail)).asSnapshot { scrollTo(2) }.size)
    }

    @Test
    fun pagingSerializesSimultaneousAppendHintsWithoutManualLocking() = runTest {
        val remote = FakeFeedRemoteDataSource()
        val gate = CompletableDeferred<Unit>()
        remote.feedReply = { cursor ->
            if (cursor != null) gate.await()
            FeedResult.Loaded(FeedTestData.feed(if (cursor == null) "feed-page-1.json" else "feed-page-2.json"))
        }
        val repository = FeedRepositoryImpl(remote, SessionStateReader { SessionState.Guest })
        val presenter = object : PagingDataPresenter<FeedSection>(StandardTestDispatcher(testScheduler)) {
            override suspend fun presentPagingDataEvent(event: PagingDataEvent<FeedSection>) = Unit
        }
        backgroundScope.launch { repository.getFeed().collectLatest(presenter::collectFrom) }
        runCurrent()
        presenter[0]
        presenter[1]
        runCurrent()
        assertEquals(1, remote.feedCalls.count { it != null })
        assertEquals(2, presenter.snapshot().items.size)
        gate.complete(Unit)
        runCurrent()
        assertEquals(3, presenter.snapshot().items.size)
    }
}
