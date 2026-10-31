package com.quizzgame.core.error.ui.common

import com.quizzgame.core.error.ErrorCopyKey
import com.quizzgame.core.error.ErrorPresentationSpec
import com.quizzgame.core.error.ErrorSurface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ErrorNoticeEventsTest {
    @Test
    fun eventsWithoutSubscriberAreNotReplayedAfterSubscriptionOrResubscription() = runTest {
        val events = ErrorNoticeEvents()
        val dropped = spec(ErrorCopyKey.CONNECTION_PROBLEM)
        val current = spec(ErrorCopyKey.REQUEST_TIMED_OUT)
        events.show(dropped)
        val first = mutableListOf<ErrorPresentationSpec>()
        val subscription = backgroundScope.launch { events.events.collect { first += it } }
        runCurrent()
        assertEquals(emptyList<ErrorPresentationSpec>(), first)
        events.show(current)
        runCurrent()
        assertEquals(listOf(current), first)
        subscription.cancel()
        runCurrent()
        val next = mutableListOf<ErrorPresentationSpec>()
        backgroundScope.launch { events.events.collect { next += it } }
        runCurrent()
        assertEquals(emptyList<ErrorPresentationSpec>(), next)
    }

    @Test
    fun slowSubscriberReceivesOnlyLatestBufferedNotice() = runTest {
        val events = ErrorNoticeEvents()
        val received = mutableListOf<ErrorPresentationSpec>()
        backgroundScope.launch { events.events.collect { received += it } }
        runCurrent()
        events.show(spec(ErrorCopyKey.CONNECTION_PROBLEM))
        val latest = spec(ErrorCopyKey.REQUEST_TIMED_OUT)
        events.show(latest)
        runCurrent()
        assertEquals(listOf(latest), received)
    }

    @Test
    fun noticeSourceRejectsEveryNonTransientSurfaceIncludingSilent() {
        ErrorSurface.entries.filter { it != ErrorSurface.TRANSIENT }.forEach { surface ->
            assertThrows(IllegalArgumentException::class.java) {
                ErrorNoticeEvents().show(spec(ErrorCopyKey.GENERIC_FAILURE).copy(surface = surface))
            }
        }
    }

    private fun spec(title: ErrorCopyKey) = ErrorPresentationSpec(ErrorSurface.TRANSIENT, title)
}
