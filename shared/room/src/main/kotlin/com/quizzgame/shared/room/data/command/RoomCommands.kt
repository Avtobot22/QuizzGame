package com.quizzgame.shared.room.data.command

import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.shared.room.data.api.RoomRemoteDataSource
import com.quizzgame.shared.room.data.storage.StoredRoom
import com.quizzgame.shared.room.domain.entity.command.RoomCommand
import com.quizzgame.shared.room.domain.entity.result.RoomFailure
import com.quizzgame.shared.room.domain.entity.result.RoomFailureReason
import com.quizzgame.shared.room.domain.entity.result.RoomResult
import com.quizzgame.shared.room.domain.entity.session.RoomRole
import com.quizzgame.shared.room.domain.entity.snapshot.RoomPhase
import com.quizzgame.shared.room.domain.entity.snapshot.RoomSnapshot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.supervisorScope
import java.util.concurrent.atomic.AtomicReference

internal class RoomCommands(private val remote: RoomRemoteDataSource) {
    private val running = AtomicReference<Job?>()

    suspend fun run(
        room: StoredRoom,
        snapshot: RoomSnapshot?,
        command: RoomCommand,
        isCurrent: suspend () -> Boolean,
    ): RoomResult<RoomSnapshot?> = supervisorScope {
        val request = async(start = CoroutineStart.LAZY) {
            if (isCurrent()) execute(room, snapshot, command) else interrupted()
        }
        running.set(request)
        try {
            request.await()
        } catch (_: CancellationException) {
            currentCoroutineContext().ensureActive()
            interrupted()
        } finally {
            running.compareAndSet(request, null)
        }
    }

    fun cancel() {
        running.get()?.cancel()
    }

    private suspend fun execute(
        room: StoredRoom,
        snapshot: RoomSnapshot?,
        command: RoomCommand,
    ): RoomResult<RoomSnapshot?> {
        if (!allowed(room.credential.role, snapshot, command)) {
            return RoomResult.Failed(RoomFailure(AppFailure.OperationInterrupted, RoomFailureReason.FORBIDDEN))
        }
        return when (command) {
            is RoomCommand.Ready -> remote.ready(room, command.value)
            is RoomCommand.Start -> when (val result = remote.start(room, command)) {
                is RoomResult.Failed -> result
                is RoomResult.Success -> RoomResult.Success(
                    snapshot?.copy(phase = RoomPhase.STARTING, version = result.value),
                )
            }
            else -> when (val result = remote.remove(room, command)) {
                is RoomResult.Failed -> result
                is RoomResult.Success -> RoomResult.Success(null)
            }
        }
    }

    private fun interrupted() = RoomResult.Failed(RoomFailure(AppFailure.OperationInterrupted))

    private fun allowed(role: RoomRole, snapshot: RoomSnapshot?, command: RoomCommand): Boolean = when (command) {
        RoomCommand.Leave -> role == RoomRole.PARTICIPANT
        RoomCommand.Close -> role == RoomRole.HOST && snapshot?.permissions?.canClose != false
        is RoomCommand.Ready ->
            role == RoomRole.PARTICIPANT && snapshot?.permissions?.canReady == true &&
                snapshot.phase == RoomPhase.LOBBY
        is RoomCommand.Kick ->
            role == RoomRole.HOST && snapshot?.permissions?.canKick == true &&
                snapshot.phase == RoomPhase.LOBBY
        is RoomCommand.Start ->
            role == RoomRole.HOST && snapshot?.permissions?.canStart == true &&
                snapshot.phase == RoomPhase.LOBBY
    }
}
