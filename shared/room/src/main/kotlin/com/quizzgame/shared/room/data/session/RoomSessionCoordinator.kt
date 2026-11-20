package com.quizzgame.shared.room.data.session

import com.quizzgame.core.coroutines.DispatcherProvider
import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.error.failure.StorageKind
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.shared.room.data.api.AcquiredRoom
import com.quizzgame.shared.room.data.api.RoomRemoteDataSource
import com.quizzgame.shared.room.data.command.RoomCommands
import com.quizzgame.shared.room.data.storage.RoomCredentialStore
import com.quizzgame.shared.room.data.storage.StoredRoom
import com.quizzgame.shared.room.data.stream.RoomConnectionLoop
import com.quizzgame.shared.room.data.stream.RoomConnectionObserver
import com.quizzgame.shared.room.data.stream.RoomStreamConnector
import com.quizzgame.shared.room.domain.entity.command.RoomCommand
import com.quizzgame.shared.room.domain.entity.result.RoomFailure
import com.quizzgame.shared.room.domain.entity.result.RoomFailureReason
import com.quizzgame.shared.room.domain.entity.result.RoomResult
import com.quizzgame.shared.room.domain.entity.session.ParticipantId
import com.quizzgame.shared.room.domain.entity.session.RoomId
import com.quizzgame.shared.room.domain.entity.session.RoomSession
import com.quizzgame.shared.room.domain.entity.session.RoomStreamState
import com.quizzgame.shared.room.domain.entity.snapshot.RoomSnapshot
import com.quizzgame.shared.room.domain.repository.RoomAccountState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class RoomSessionCoordinator @Inject constructor(
    private val store: RoomCredentialStore,
    remote: RoomRemoteDataSource,
    connector: RoomStreamConnector,
    accounts: RoomAccountState,
    dispatchers: DispatcherProvider,
) {

    private val scope = CoroutineScope(SupervisorJob() + dispatchers.io)
    private val accountState = accounts.state()
    private val mutations = Mutex()
    private val lock = Mutex()
    private val mutableSession = MutableStateFlow<RoomSession?>(null)
    private val connection = RoomConnectionLoop(remote::snapshot, connector::observe)
    private var room: ActiveRoom? = null
        set(value) {
            field = value
            mutableSession.value = value?.saved?.session
        }
    private var hydrated = false
    private var stream: Job? = null
    private var connectionEpoch = 0L
    private val commands = RoomCommands(remote)

    val session = mutableSession.asStateFlow()

    init {
        scope.launch {
            accountState.collect { account ->
                lock.withLock {
                    val current = room ?: return@withLock
                    val owner = current.saved.accountGeneration
                    if (owner != null && account.conflictsWith(owner)) {
                        end(current, RoomFailureReason.ACCOUNT_CHANGED)
                    }
                }
            }
        }
    }

    suspend fun restore(): RoomResult<RoomSession?> = lock.withLock { restoreLocked() }

    private suspend fun restoreLocked(): RoomResult<RoomSession?> =
        try {
            if (!hydrated) {
                val saved = store.load()
                if (saved != null && saved.invalidReason(accountState.value) == null) {
                    room = ActiveRoom(saved, null)
                } else if (saved != null) store.clear()
                hydrated = true
            }
            val current = room
            current?.saved?.invalidReason(accountState.value)?.let { end(current, it) }
            RoomResult.Success(room?.saved?.session)
        } catch (_: IOException) {
            storageFailure(StorageKind.READ)
        }
    suspend fun enterLobby(id: RoomId): RoomResult<RoomLobbySubscription> = lock.withLock {
        val restored = restoreLocked()
        if (restored is RoomResult.Failed) return@withLock restored
        val current = room?.takeIf { it.saved.id == id } ?: return@withLock expiredRoom()
        current.attendance++
        startConnection(current)
        RoomResult.Success(current.subscription)
    }

    suspend fun exitLobby(subscription: RoomLobbySubscription) = lock.withLock {
        val current = room?.takeIf { it.subscription === subscription } ?: return@withLock
        current.attendance--
        if (current.attendance == 0) {
            stopConnection()
            current.updateConnectionState(RoomStreamState.Reconnecting(current.snapshot))
        }
    }

    internal suspend fun persist(acquired: AcquiredRoom): RoomResult<RoomSession> = withContext(NonCancellable) {
        lock.withLock {
            val saved = acquired.room
            saved.invalidReason(accountState.value)?.let { return@withLock endedRoom(it) }
            try {
                store.save(saved)
                val invalid = saved.invalidReason(accountState.value)
                if (invalid == null) {
                    room = ActiveRoom(saved, acquired.snapshot)
                    RoomResult.Success(saved.session)
                } else {
                    store.clear()
                    endedRoom(invalid)
                }
            } catch (_: IOException) {
                storageFailure(StorageKind.WRITE)
            }
        }
    }

    private fun startConnection(current: ActiveRoom) {
        if (stream?.isActive == true) return
        val epoch = ++connectionEpoch
        val observer = object : RoomConnectionObserver {
            private fun currentConnection(): Boolean =
                room === current && connectionEpoch == epoch && current.isValid(accountState.value)

            override suspend fun lastSnapshot(): RoomSnapshot? = lock.withLock {
                current.snapshot.takeIf { currentConnection() }
            }

            override suspend fun publish(value: RoomSnapshot, connected: Boolean) = lock.withLock {
                if (currentConnection()) current.applySnapshot(value, connected)
            }

            override suspend fun update(state: RoomStreamState) = lock.withLock {
                if (currentConnection()) current.updateConnectionState(state)
            }

            override suspend fun end(reason: RoomFailureReason) = lock.withLock {
                if (room === current && connectionEpoch == epoch) end(current, reason)
            }
        }
        stream = scope.launch { connection.run(current.saved, observer) }
    }

    suspend fun sendCommand(id: RoomId, command: RoomCommand): RoomResult<Unit> = mutations.withLock {
        val admitted = lock.withLock { room?.takeIf { it.saved.id == id && it.isValid(accountState.value) } }
            ?: return@withLock expiredRoom()
        val active = (admitted.state.value as? RoomStreamState.Active)?.snapshot
        val result = commands.run(admitted.saved, active, command) {
            lock.withLock { admitted === room && admitted.isValid(accountState.value) }
        }
        lock.withLock commit@{
            if (admitted !== room || !admitted.isValid(accountState.value)) {
                return@commit closedCommandResult(command, admitted.state.value)
            }
            applyCommandResult(admitted, command, result)
        }
    }

    private suspend fun applyCommandResult(
        admitted: ActiveRoom,
        command: RoomCommand,
        result: RoomResult<RoomSnapshot?>,
    ): RoomResult<Unit> = when (result) {
        is RoomResult.Failed -> {
            val failure = result.failure
            if (failure.terminal) {
                end(admitted, requireNotNull(failure.reason))
            } else if (failure.reason in CONFLICTS) {
                stopConnection()
                admitted.updateConnectionState(RoomStreamState.Reconnecting(admitted.snapshot))
                if (admitted.attendance > 0) startConnection(admitted)
            }
            result
        }

        is RoomResult.Success -> {
            when (command) {
                RoomCommand.Close, RoomCommand.Leave -> end(admitted, RoomFailureReason.CLOSED)
                else -> result.value?.let {
                    admitted.applySnapshot(it, admitted.state.value is RoomStreamState.Active)
                }
            }
            RoomResult.Success(Unit)
        }
    }

    private suspend fun end(current: ActiveRoom, reason: RoomFailureReason) = withContext(NonCancellable) {
        if (reason != RoomFailureReason.CLOSED) commands.cancel()
        room = null
        stopConnection()
        current.state.value = try {
            store.clear()
            RoomStreamState.Ended(current.saved.id, reason)
        } catch (_: IOException) {
            RoomStreamState.Failed(storageFailure(StorageKind.WRITE).failure)
        }
    }

    private fun stopConnection() {
        connectionEpoch++
        stream?.cancel()
        stream = null
    }

    private class ActiveRoom(val saved: StoredRoom, var snapshot: RoomSnapshot?) {
        val state = MutableStateFlow<RoomStreamState>(RoomStreamState.Connecting)
        val subscription = RoomLobbySubscription(saved.self, state.asStateFlow())
        var attendance = 0

        fun isValid(account: SessionState): Boolean = saved.invalidReason(account) == null

        fun applySnapshot(value: RoomSnapshot, connected: Boolean) {
            val previous = snapshot
            val latest = if (previous == null || value.version > previous.version) value else previous
            snapshot = latest
            updateConnectionState(
                if (connected) RoomStreamState.Active(latest) else RoomStreamState.Reconnecting(latest),
            )
        }

        fun updateConnectionState(next: RoomStreamState) {
            if (state.value !is RoomStreamState.Started) state.value = next
        }
    }

    private companion object {

        val CONFLICTS = setOf(RoomFailureReason.STATE_CONFLICT, RoomFailureReason.VERSION_CONFLICT)
    }
}

internal class RoomLobbySubscription(val self: ParticipantId?, val states: StateFlow<RoomStreamState>)

private fun StoredRoom.invalidReason(account: SessionState): RoomFailureReason? = when {
    accountGeneration != null && account.conflictsWith(accountGeneration) -> RoomFailureReason.ACCOUNT_CHANGED
    !credential.expiresAt.isAfter(Instant.now()) -> RoomFailureReason.EXPIRED
    else -> null
}

private fun SessionState.conflictsWith(owner: Long): Boolean = when (this) {
    is SessionState.Authenticated                                 -> sessionGeneration != owner
    SessionState.Guest, SessionState.Expired, SessionState.Closed -> true
    is SessionState.Blocked                                       -> lastKnown.hasDifferentOwner(owner)
    is SessionState.Unavailable                                   -> lastKnown.hasDifferentOwner(owner)
    else                                                          -> false
}

private fun SessionState.Ready?.hasDifferentOwner(owner: Long): Boolean =
    this != null && this != SessionState.Authenticated(owner)

internal fun storageFailure(kind: StorageKind) = RoomResult.Failed(
    RoomFailure(AppFailure.Storage(kind), uncertain = true),
)

private fun expiredRoom() = endedRoom(RoomFailureReason.EXPIRED)

private fun endedRoom(reason: RoomFailureReason) =
    RoomResult.Failed(RoomFailure(AppFailure.OperationInterrupted, reason))

private fun closedCommandResult(command: RoomCommand, state: RoomStreamState): RoomResult<Unit> {
    val ended = state as? RoomStreamState.Ended
    val exiting = command == RoomCommand.Close || command == RoomCommand.Leave
    return if (exiting && ended?.reason == RoomFailureReason.CLOSED) {
        RoomResult.Success(Unit)
    } else {
        expiredRoom()
    }
}
