package com.quizzgame.shared.room.data

import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.network.config.createNetworkJson
import com.quizzgame.shared.room.data.dto.admission.CreateRoomResponse
import com.quizzgame.shared.room.data.mapper.toDomain
import com.quizzgame.shared.room.data.storage.RoomCredential
import com.quizzgame.shared.room.data.storage.StoredRoom
import com.quizzgame.shared.room.data.stream.RoomConnectionLoop
import com.quizzgame.shared.room.data.stream.RoomConnectionObserver
import com.quizzgame.shared.room.data.stream.RoomStreamEvent
import com.quizzgame.shared.room.domain.entity.admission.RoomEndpointSettings
import com.quizzgame.shared.room.domain.entity.admission.RoomJoinLink
import com.quizzgame.shared.room.domain.entity.result.RoomFailureReason
import com.quizzgame.shared.room.domain.entity.result.RoomResult
import com.quizzgame.shared.room.domain.entity.session.RoomRole
import com.quizzgame.shared.room.domain.entity.session.RoomStreamState
import com.quizzgame.shared.room.domain.entity.snapshot.RoomSnapshot
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class RoomConnectionLoopTest {
    private val host = createNetworkJson().decodeFromString<CreateRoomResponse>(fixture("tv-create-response"))
    private val links = RoomJoinLink(RoomEndpointSettings("join.example.test"))
    private val first = host.room.toDomain(RoomRole.HOST, links)
    private val stored = StoredRoom(
        first.roomId,
        RoomCredential(RoomRole.HOST, "fixture", Instant.now().plus(1, ChronoUnit.DAYS)),
        null,
        null,
    )

    @Test
    fun reconnectsAfterMissingInitialAndIgnoresStaleSnapshots() = runTest {
        val observer = Observer(first)
        val lastSeenVersions = mutableListOf<Long?>()
        val latest = first.copy(version = 4)
        val loop = RoomConnectionLoop({ RoomResult.Success(first.copy(version = 2)) }) { _, since ->
            lastSeenVersions += since
            when (lastSeenVersions.size) {
                1 -> emptyFlow()
                2 -> flowOf(RoomStreamEvent.Snapshot(first))
                else -> flowOf(
                    RoomStreamEvent.Snapshot(latest),
                    RoomStreamEvent.Snapshot(first.copy(version = 3)),
                    RoomStreamEvent.Ended(3, RoomFailureReason.REMOVED),
                    RoomStreamEvent.Ended(5, RoomFailureReason.CLOSED),
                )
            }
        }
        loop.run(stored, observer)
        assertEquals(listOf(1L, 2L, 2L), lastSeenVersions)
        assertEquals(latest, observer.lastSnapshot())
        assertEquals(listOf(RoomStreamState.Active(latest)), observer.states.filterIsInstance<RoomStreamState.Active>())
        assertTrue(observer.states.any { it is RoomStreamState.Reconnecting })
        assertEquals(listOf(RoomFailureReason.CLOSED), observer.terminal)
    }

    @Test
    fun stopsReconnectingWhenTheServerNoLongerHasTheSavedRoom() = runTest {
        val observer = Observer(first)
        val loop = RoomConnectionLoop(
            { RoomResult.Failed(RoomFailure(AppFailure.OperationInterrupted, RoomFailureReason.NOT_FOUND)) },
            { _, _ -> emptyFlow() },
        )

        loop.run(stored, observer)

        assertEquals(listOf(RoomFailureReason.NOT_FOUND), observer.terminal)
    }

    @Test
    fun cancellationClosesSubscriptionWithoutAnExpiryTransition() = runTest {
        val observer = Observer(first)
        var closed = false
        val loop = RoomConnectionLoop({ RoomResult.Success(first) }) { _, _ ->
            flow {
                try {
                    emit(RoomStreamEvent.Snapshot(first))
                    awaitCancellation()
                } finally {
                    closed = true
                }
            }
        }
        val listening = launch { loop.run(stored, observer) }
        runCurrent()
        listening.cancelAndJoin()
        assertTrue(closed)
        assertTrue(observer.terminal.isEmpty())
    }

    private class Observer(private var latest: RoomSnapshot) : RoomConnectionObserver {
        val states = mutableListOf<RoomStreamState>()
        val terminal = mutableListOf<RoomFailureReason>()

        override suspend fun lastSnapshot(): RoomSnapshot = latest

        override suspend fun publish(value: RoomSnapshot, connected: Boolean) {
            if (value.version > latest.version) latest = value
            update(if (connected) RoomStreamState.Active(latest) else RoomStreamState.Reconnecting(latest))
        }

        override suspend fun update(state: RoomStreamState) {
            states += state
        }

        override suspend fun end(reason: RoomFailureReason) {
            terminal += reason
        }
    }
}
import com.quizzgame.shared.room.domain.entity.result.RoomFailure
