package com.quizzgame.shared.room.data.stream

import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.shared.room.data.storage.StoredRoom
import com.quizzgame.shared.room.domain.entity.result.RoomFailure
import com.quizzgame.shared.room.domain.entity.result.RoomFailureReason
import com.quizzgame.shared.room.domain.entity.result.RoomResult
import com.quizzgame.shared.room.domain.entity.session.RoomStreamState
import com.quizzgame.shared.room.domain.entity.snapshot.RoomPhase
import com.quizzgame.shared.room.domain.entity.snapshot.RoomSnapshot
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

internal class RoomConnectionLoop(
    private val fetchSnapshot: suspend (StoredRoom) -> RoomResult<RoomSnapshot>,
    private val observe: (StoredRoom, Long?) -> Flow<RoomStreamEvent>,
) {
    suspend fun run(room: StoredRoom, observer: RoomConnectionObserver, reconcileFirst: Boolean) {
        var retry = 0
        var refresh = reconcileFirst
        while (true) {
            if (refresh && !reconcile(room, observer)) return
            when (listen(room, observer)) {
                ConnectionOutcome.ENDED -> return
                ConnectionOutcome.RETRY_AFTER_ACTIVE -> retry = 0
                ConnectionOutcome.RETRY -> Unit
            }
            observer.update(RoomStreamState.Reconnecting(observer.lastSnapshot()))
            delay(retryDelay(retry).milliseconds)
            retry = (retry + 1).coerceAtMost(BACKOFF.lastIndex)
            refresh = true
        }
    }

    private suspend fun reconcile(room: StoredRoom, observer: RoomConnectionObserver): Boolean =
        when (val result = fetchSnapshot(room)) {
            is RoomResult.Success -> !applySnapshot(result.value, room, observer, connected = false)
            is RoomResult.Failed -> !result.failure.terminal || !observer.end(requireNotNull(result.failure.reason))
        }

    private suspend fun listen(room: StoredRoom, observer: RoomConnectionObserver): ConnectionOutcome = coroutineScope {
        val events = observe(room, observer.lastVersion().takeIf { it >= 0 }).produceIn(this)
        try {
            val initial = withTimeoutOrNull(INITIAL_SNAPSHOT_TIMEOUT.milliseconds) {
                events.receiveCatching().getOrNull()
            }
            if (initial !is RoomStreamEvent.Snapshot || initial.value.roomId != room.id) {
                return@coroutineScope handleMissingInitial(initial, observer)
            }
            if (initial.value.version < observer.lastVersion()) {
                return@coroutineScope ConnectionOutcome.RETRY
            }
            if (handle(initial, room, observer)) return@coroutineScope ConnectionOutcome.ENDED
            for (event in events) {
                if (event == RoomStreamEvent.Invalid) return@coroutineScope ConnectionOutcome.RETRY_AFTER_ACTIVE
                if (handle(event, room, observer)) return@coroutineScope ConnectionOutcome.ENDED
            }
            ConnectionOutcome.RETRY_AFTER_ACTIVE
        } finally {
            events.cancel()
        }
    }

    private suspend fun handleMissingInitial(
        initial: RoomStreamEvent?,
        observer: RoomConnectionObserver,
    ): ConnectionOutcome {
        if (initial is RoomStreamEvent.Ended && observer.end(initial.reason, initial.version)) {
            return ConnectionOutcome.ENDED
        }
        if (initial != null && observer.lastSnapshot() == null) {
            observer.update(RoomStreamState.Failed(RoomFailure(AppFailure.InvalidServerData)))
        }
        return ConnectionOutcome.RETRY
    }

    private suspend fun handle(
        event: RoomStreamEvent,
        room: StoredRoom,
        observer: RoomConnectionObserver,
    ): Boolean = when (event) {
        is RoomStreamEvent.Snapshot -> applySnapshot(event.value, room, observer, connected = true)
        is RoomStreamEvent.Ended -> observer.end(event.reason, event.version)
        is RoomStreamEvent.Started -> {
            observer.started(event)
            false
        }
        RoomStreamEvent.Invalid -> false
    }

    private suspend fun applySnapshot(
        snapshot: RoomSnapshot,
        room: StoredRoom,
        observer: RoomConnectionObserver,
        connected: Boolean,
    ): Boolean {
        if (snapshot.roomId != room.id) return false
        if (snapshot.phase == RoomPhase.CLOSED) return observer.end(RoomFailureReason.CLOSED, snapshot.version)
        observer.publish(snapshot, connected)
        return false
    }

    private fun retryDelay(attempt: Int): Long = (BACKOFF[attempt] * Random.nextDouble(JITTER_MIN, JITTER_MAX)).toLong()

    private enum class ConnectionOutcome { ENDED, RETRY, RETRY_AFTER_ACTIVE }

    private companion object {
        const val JITTER_MIN = 0.8
        const val JITTER_MAX = 1.2
        const val INITIAL_SNAPSHOT_TIMEOUT = 10_000L
        val BACKOFF = listOf(1_000L, 2_000L, 4_000L, 8_000L, 16_000L, 30_000L)
    }
}
