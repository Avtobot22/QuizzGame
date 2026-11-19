package com.quizzgame.shared.feed.data.paging

import androidx.paging.PagingConfig
import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import androidx.paging.PagingState
import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.shared.catalog.domain.entity.CursorPageInfo
import com.quizzgame.shared.catalog.domain.entity.QuizCard
import com.quizzgame.shared.feed.FakeFeedRemoteDataSource
import com.quizzgame.shared.feed.FeedTestData
import com.quizzgame.shared.feed.domain.entity.Feed
import com.quizzgame.shared.feed.domain.entity.FeedPagingException
import com.quizzgame.shared.feed.domain.entity.FeedResult
import com.quizzgame.shared.feed.domain.entity.section.FeedSection
import com.quizzgame.shared.feed.domain.entity.section.QuizRail
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
        assertEquals(
            FeedTestData.feed().sections.filterIsInstance<QuizRail>() +
                FeedTestData.feed("feed-page-2.json").sections.filterIsInstance<QuizRail>(),
            first.data + last.data,
        )
        assertNull(last.nextKey)
        assertNull(first.prevKey)
    }

    @Test
    fun inlinePreviewIsEmittedWithoutHttpAndContinuationStartsAfterIt() = runTest {
        val rail = FeedTestData.feed().sections.filterIsInstance<QuizRail>().first()
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
    fun failedAppendCanRetryWithoutDroppingPreviewOrPoisoningIds() = runTest {
        val source = RailItemsPagingSource(
            FeedTestData.feed().sections.filterIsInstance<QuizRail>().first(),
            repository,
            owner,
        )
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
        val rail = FeedTestData.feed().sections.filterIsInstance<QuizRail>().first()
        val source = RailItemsPagingSource(rail, repository, owner)
        source.load(LoadParams.Refresh(null, 12, false))
        repository.itemsReply = { FeedResult.Loaded(FeedTestData.items().copy(cards = rail.cards)) }
        assertTrue(
            source.load(LoadParams.Append(CursorKey.After("opaque-rail-a-after-2"), 12, false)) is LoadResult.Error,
        )
        repository.itemsReply = {
            FeedResult.Loaded(FeedTestData.items().copy(page = CursorPageInfo(true, "opaque-rail-a-after-2")))
        }
        assertTrue(
            source.load(LoadParams.Append(CursorKey.After("opaque-rail-a-after-2"), 12, false)) is LoadResult.Error,
        )
    }

    @Test
    fun duplicateSectionAndEarlierCursorCycleAreRejected() = runTest {
        val source = FeedSectionsPagingSource(repository, owner)
        source.load(LoadParams.Refresh(null, 6, false))
        repository.feedReply = { FeedResult.Loaded(FeedTestData.feed()) }
        assertTrue(
            source.load(
                LoadParams.Append(CursorKey.After("opaque-feed-after-2-sections"), 6, false),
            ) is LoadResult.Error,
        )
    }

    @Test
    fun emptyTerminalPagesStopBothAxes() = runTest {
        repository.feedReply = { FeedResult.Loaded(Feed(emptyList())) }
        val source = FeedSectionsPagingSource(repository, owner)
        val empty = source.load(LoadParams.Refresh(null, 6, false)) as LoadResult.Page
        assertTrue(empty.data.isEmpty())
        assertNull(empty.nextKey)
        val rail = FeedTestData.feed().sections.filterIsInstance<QuizRail>().first()
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
        val rail = FeedTestData.feed().sections.filterIsInstance<QuizRail>().first()
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

    @Test
    fun failedInitialLoadIsAnErrorAndCanBeRetried() = runTest {
        repository.feedReply = { FeedResult.Failed(AppFailure.InvalidServerData) }
        val source = FeedSectionsPagingSource(repository, owner)
        assertTrue(source.load(LoadParams.Refresh(null, 6, false)) is LoadResult.Error)
        repository.feedReply = { FeedResult.Loaded(FeedTestData.feed()) }
        assertTrue(source.load(LoadParams.Refresh(null, 6, false)) is LoadResult.Page)
        assertEquals(listOf(null, null), repository.feedCalls)
    }

    @Test
    fun unexpectedRequestExceptionBecomesAnErrorPage() = runTest {
        val failure = java.io.IOException("Unavailable")
        repository.feedReply = { throw failure }
        val source = FeedSectionsPagingSource(repository, owner)
        val error = source.load(LoadParams.Refresh(null, 6, false)) as LoadResult.Error
        assertEquals(failure, error.throwable)
    }

    @Test
    fun refreshKeyDoesNotReuseAnAnchoredCursor() {
        val source = FeedSectionsPagingSource(repository, owner)
        val page = LoadResult.Page<CursorKey, FeedSection>(
            FeedTestData.feed().sections.filterIsInstance<QuizRail>(),
            null,
            CursorKey.After("opaque"),
        )
        assertNull(source.getRefreshKey(PagingState(listOf(page), 1, PagingConfig(6), 0)))
    }

    @Test
    fun anEarlierCursorCycleWithDistinctCardsIsRejected() = runTest {
        val rail = FeedTestData.feed().sections.filterIsInstance<QuizRail>().first()
        val source = RailItemsPagingSource(rail, repository, owner)
        source.load(LoadParams.Refresh(null, 12, false))
        repository.itemsReply = {
            FeedResult.Loaded(FeedTestData.items().copy(page = CursorPageInfo(true, "third")))
        }
        source.load(LoadParams.Append(CursorKey.After("opaque-rail-a-after-2"), 12, false))
        repository.itemsReply = {
            FeedResult.Loaded(
                FeedTestData.items().copy(
                    cards = listOf(rail.cards.first().copy(id = "new-card")),
                    page = CursorPageInfo(true, "opaque-rail-a-after-2"),
                ),
            )
        }
        assertTrue(source.load(LoadParams.Append(CursorKey.After("third"), 12, false)) is LoadResult.Error)
    }

    @Test
    fun droppedPageCanBeLoadedBackWithoutLosingInlinePreviewOrSnapshot() = runTest {
        val source = FeedSectionsPagingSource(repository, owner, beginSnapshot = { 7L })
        val first = source.load(LoadParams.Refresh(null, 5, false)) as LoadResult.Page
        val second = source.load(LoadParams.Append(checkNotNull(first.nextKey), 5, false)) as LoadResult.Page
        assertEquals(CursorKey.Start, second.prevKey)
        val restored = source.load(LoadParams.Prepend(checkNotNull(second.prevKey), 5, false)) as LoadResult.Page
        assertEquals(first.data, restored.data)
        assertEquals(first.nextKey, restored.nextKey)
        assertNull(restored.prevKey)
        assertEquals(listOf(null, "opaque-feed-after-2-sections", null), repository.feedCalls)
    }

    @Test
    fun obsoleteRailDoesNotSendItsCursor() = runTest {
        val rail = FeedTestData.feed().sections.filterIsInstance<QuizRail>().first()
        val source = RailItemsPagingSource(rail, repository, owner, isCurrent = { false })
        val error = source.load(LoadParams.Append(CursorKey.After("obsolete"), 12, false)) as LoadResult.Error
        assertEquals(AppFailure.OperationInterrupted, (error.throwable as FeedPagingException).failure)
        assertTrue(repository.itemCalls.isEmpty())
    }
}
