package com.quizzgame.feature.feed.tv.data

import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import androidx.paging.testing.asSnapshot
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.core.session.lifecycle.SessionStateReader
import com.quizzgame.feature.feed.tv.FakeFeedRemoteDataSource
import com.quizzgame.feature.feed.tv.FeedTestData
import com.quizzgame.feature.feed.tv.data.paging.FeedPagingSession
import com.quizzgame.feature.feed.tv.domain.entity.FeedResult
import com.quizzgame.feature.feed.tv.domain.entity.QuizRail
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
    fun railCollectorsReuseDataAndDoNotFetchThePreviewAgain() = runTest {
        val remote = FakeFeedRemoteDataSource()
        val repository = FeedRepositoryImpl(remote, SessionStateReader { SessionState.Guest })
        val rails = repository.load(backgroundScope).asSnapshot { scrollTo(2) }
        assertTrue(remote.itemCalls.isEmpty())
        val first = repository.loadItems(rails.first()).asSnapshot { scrollTo(2) }
        val repeated = repository.loadItems(rails.first()).asSnapshot { scrollTo(2) }
        assertEquals(first, repeated)
        assertEquals(1, remote.itemCalls.size)
    }

    @Test
    fun newViewModelRepositoryDoesNotReusePreviousOwnerPages() = runTest {
        val remote = FakeFeedRemoteDataSource()
        val previous = FeedRepositoryImpl(remote, SessionStateReader { SessionState.Authenticated(1) })
        val old = previous.load(backgroundScope).asSnapshot { scrollTo(2) }
        val repository = FeedRepositoryImpl(remote, SessionStateReader { SessionState.Authenticated(2) })
        val current = repository.load(backgroundScope).asSnapshot { scrollTo(2) }
        assertNotEquals(old.first().snapshotId, current.first().snapshotId)
        assertTrue(repository.loadItems(old.first()).asSnapshot().isEmpty())
        assertTrue(remote.itemCalls.isEmpty())
        assertEquals(3, repository.loadItems(current.first()).asSnapshot { scrollTo(2) }.size)
    }

    @Test
    fun explicitRefreshRenewsTheOneAutomaticExpirationRecovery() = runTest {
        val remote = FakeFeedRemoteDataSource()
        remote.feedReply = { cursor ->
            if (cursor == null) FeedResult.Loaded(FeedTestData.feed()) else FeedResult.Expired
        }
        val repository = FeedRepositoryImpl(remote, SessionStateReader { SessionState.Guest })
        val presenter = object : PagingDataPresenter<QuizRail>(StandardTestDispatcher(testScheduler)) {
            override suspend fun presentPagingDataEvent(event: PagingDataEvent<QuizRail>) = Unit
        }
        backgroundScope.launch {
            repository.load(backgroundScope).collectLatest(presenter::collectFrom)
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
            backgroundScope,
        )
        val failure = runCatching { paging.sections.asSnapshot { scrollTo(2) } }
        assertTrue(failure.isFailure)
        assertEquals(2, remote.feedCalls.count { it == null })
        assertEquals(2, remote.feedCalls.count { it != null })
        paging.close()
    }
}
