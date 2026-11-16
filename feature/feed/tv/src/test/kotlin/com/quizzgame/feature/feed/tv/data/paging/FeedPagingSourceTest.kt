package com.quizzgame.feature.feed.tv.data.paging

import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.feature.feed.tv.FakeFeedRemoteDataSource
import com.quizzgame.feature.feed.tv.FeedTestData
import com.quizzgame.feature.feed.tv.domain.FeedPagingException
import com.quizzgame.feature.feed.tv.domain.entity.CursorPageInfo
import com.quizzgame.feature.feed.tv.domain.entity.Feed
import com.quizzgame.feature.feed.tv.domain.entity.FeedResult
import com.quizzgame.feature.feed.tv.domain.entity.QuizCard
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeedPagingSourceTest {
    private val repository = FakeFeedRemoteDataSource()
    private val owner = SessionState.Guest

    @Test
    fun sectionsAppendUsesOpaqueCursorAndKeepsOrder() = runTest {
        val source = FeedSectionsPagingSource(repository, owner)
        val first = source.load(LoadParams.Refresh(null, 6, false)) as LoadResult.Page
        val last = source.load(LoadParams.Append(checkNotNull(first.nextKey), 6, false)) as LoadResult.Page
        assertEquals(listOf(null, "opaque-feed-after-2-sections"), repository.feedCalls)
        assertEquals(FeedTestData.feed().rails + FeedTestData.feed("feed-page-2.json").rails, first.data + last.data)
        assertNull(last.nextKey)
        assertNull(first.prevKey)
    }

    @Test
    fun inlinePreviewIsEmittedWithoutHttpAndContinuationStartsAfterIt() = runTest {
        val rail = FeedTestData.feed().rails.first()
        val source = RailItemsPagingSource(rail, repository, owner)
        val first = source.load(LoadParams.Refresh(null, 12, false)) as LoadResult.Page
        assertEquals(rail.cards, first.data)
        assertTrue(repository.itemCalls.isEmpty())
        val last = source.load(LoadParams.Append(checkNotNull(first.nextKey), 12, false)) as LoadResult.Page
        assertEquals(listOf(rail.id to "opaque-rail-a-after-2"), repository.itemCalls)
        assertEquals(3, (first.data + last.data).map { it.id }.distinct().size)
        assertNull(last.nextKey)
    }

    @Test
    fun concurrentSameKeyLoadsHaveOneRequestAndOneStablePage() = runTest {
        val source = RailItemsPagingSource(FeedTestData.feed().rails.first(), repository, owner)
        source.load(LoadParams.Refresh(null, 12, false))
        val gate = CompletableDeferred<Unit>()
        repository.itemsReply = {
            gate.await()
            FeedResult.Loaded(FeedTestData.items())
        }
        val first = async { source.load(LoadParams.Append("opaque-rail-a-after-2", 12, false)) }
        val second = async { source.load(LoadParams.Append("opaque-rail-a-after-2", 12, false)) }
        runCurrent()
        assertEquals(1, repository.itemCalls.size)
        gate.complete(Unit)
        assertEquals(first.await(), second.await())
    }

    @Test
    fun failedAppendCanRetryWithoutDroppingPreviewOrPoisoningIds() = runTest {
        val source = RailItemsPagingSource(FeedTestData.feed().rails.first(), repository, owner)
        val first = source.load(LoadParams.Refresh(null, 12, false)) as LoadResult.Page
        repository.itemsReply = { FeedResult.Failed(AppFailure.InvalidServerData) }
        val key = checkNotNull(first.nextKey)
        assertTrue(source.load(LoadParams.Append(key, 12, false)) is LoadResult.Error)
        repository.itemsReply = { FeedResult.Loaded(FeedTestData.items()) }
        assertTrue(source.load(LoadParams.Append(key, 12, false)) is LoadResult.Page)
        assertEquals(2, first.data.size)
    }

    @Test
    fun duplicateCardAndCursorCycleAreContractErrors() = runTest {
        val rail = FeedTestData.feed().rails.first()
        val source = RailItemsPagingSource(rail, repository, owner)
        source.load(LoadParams.Refresh(null, 12, false))
        repository.itemsReply = { FeedResult.Loaded(FeedTestData.items().copy(cards = rail.cards)) }
        assertTrue(source.load(LoadParams.Append("opaque-rail-a-after-2", 12, false)) is LoadResult.Error)
        repository.itemsReply = {
            FeedResult.Loaded(FeedTestData.items().copy(page = CursorPageInfo(true, "opaque-rail-a-after-2")))
        }
        assertTrue(source.load(LoadParams.Append("opaque-rail-a-after-2", 12, false)) is LoadResult.Error)
    }

    @Test
    fun duplicateSectionAndEarlierCursorCycleAreRejected() = runTest {
        val source = FeedSectionsPagingSource(repository, owner)
        source.load(LoadParams.Refresh(null, 6, false))
        repository.feedReply = { FeedResult.Loaded(FeedTestData.feed()) }
        assertTrue(source.load(LoadParams.Append("opaque-feed-after-2-sections", 6, false)) is LoadResult.Error)
    }

    @Test
    fun emptyTerminalPagesStopBothAxes() = runTest {
        repository.feedReply = { FeedResult.Loaded(Feed(emptyList())) }
        val source = FeedSectionsPagingSource(repository, owner)
        val empty = source.load(LoadParams.Refresh(null, 6, false)) as LoadResult.Page
        assertTrue(empty.data.isEmpty())
        assertNull(empty.nextKey)
        val rail = FeedTestData.feed().rails.first()
        val items = RailItemsPagingSource(rail, repository, owner)
        val first = items.load(LoadParams.Refresh(null, 12, false)) as LoadResult.Page
        repository.itemsReply = { FeedResult.Loaded(FeedTestData.items().copy(cards = emptyList())) }
        val last = items.load(LoadParams.Append(checkNotNull(first.nextKey), 12, false)) as LoadResult.Page
        assertTrue(last.data.isEmpty())
        assertNull(last.nextKey)
    }

    @Test
    fun cancellationPropagatesAndDoesNotBecomeAnErrorPage() = runTest {
        repository.feedReply = { awaitCancellation() }
        val source = FeedSectionsPagingSource(repository, owner)
        var returned = false
        val job = launch {
            source.load(LoadParams.Refresh(null, 6, false))
            returned = true
        }
        runCurrent()
        job.cancelAndJoin()
        assertFalse(returned)
        assertFalse(source.invalid)
    }

    @Test
    fun expirationInvalidatesOnceWhenRecoveryIsAccepted() = runTest {
        repository.feedReply = { FeedResult.Expired }
        var resets = 0
        val source = FeedSectionsPagingSource(repository, owner, onExpired = {
            resets++
            true
        })
        assertTrue(source.load(LoadParams.Refresh(null, 6, false)) is LoadResult.Invalid)
        assertTrue(source.load(LoadParams.Refresh(null, 6, false)) is LoadResult.Invalid)
        assertEquals(1, resets)
    }

    @Test
    fun rejectedRecoveryIsVisibleAsTypedError() = runTest {
        repository.feedReply = { FeedResult.Expired }
        val source = FeedSectionsPagingSource(repository, owner)
        val error = source.load(LoadParams.Refresh(null, 6, false)) as LoadResult.Error
        assertEquals(AppFailure.InvalidServerData, (error.throwable as FeedPagingException).failure)
    }

    @Test
    fun threeRailPagesKeepSameEntryPlacementsAndFinish() = runTest {
        val rail = FeedTestData.feed().rails.first()
        val source = RailItemsPagingSource(rail, repository, owner)
        val first = source.load(LoadParams.Refresh(null, 12, false)) as LoadResult.Page
        repository.itemsReply = { FeedResult.Loaded(FeedTestData.items().copy(page = CursorPageInfo(true, "third"))) }
        val second = source.load(LoadParams.Append(checkNotNull(first.nextKey), 12, false)) as LoadResult.Page
        repository.itemsReply = {
            val card = rail.cards.first().copy(id = "another-snippet")
            FeedResult.Loaded(FeedTestData.items().copy(cards = listOf(card)))
        }
        val third = source.load(LoadParams.Append(checkNotNull(second.nextKey), 12, false)) as LoadResult.Page
        val cards = first.data + second.data + third.data
        assertEquals(4, cards.map(QuizCard::id).distinct().size)
        assertEquals(first.data.first().entryId, third.data.single().entryId)
        assertNull(third.nextKey)
    }
}
