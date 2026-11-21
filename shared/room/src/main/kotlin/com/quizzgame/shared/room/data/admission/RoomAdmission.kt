package com.quizzgame.shared.room.data.admission

import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.error.failure.StorageKind
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.shared.room.data.api.JoinAdmission
import com.quizzgame.shared.room.data.api.RoomRemoteDataSource
import com.quizzgame.shared.room.data.session.RoomSessionCoordinator
import com.quizzgame.shared.room.data.session.storageFailure
import com.quizzgame.shared.room.data.storage.RoomCredentialStore
import com.quizzgame.shared.room.domain.entity.admission.RoomJoinInput
import com.quizzgame.shared.room.domain.entity.result.RoomFailure
import com.quizzgame.shared.room.domain.entity.result.RoomFailureReason
import com.quizzgame.shared.room.domain.entity.result.RoomResult
import com.quizzgame.shared.room.domain.entity.session.RoomSession
import com.quizzgame.shared.room.domain.entity.session.roomUuid
import com.quizzgame.shared.room.domain.repository.RoomAccountState
import com.quizzgame.shared.room.domain.repository.RoomRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class RoomAdmission @Inject constructor(
    private val coordinator: RoomSessionCoordinator,
    private val remote: RoomRemoteDataSource,
    private val store: RoomCredentialStore,
    private val accounts: RoomAccountState,
) : RoomRepository {
    private val admission = Mutex()
    private var pending: Attempt? = null

    override suspend fun create(entryId: String): RoomResult<RoomSession> {
        val entry = entryId.roomUuid() ?: return invalidInput()
        return acquire(Request.Create(entry))
    }

    override suspend fun join(
        code: String,
        nickname: String,
        accountGeneration: Long?,
    ): RoomResult<RoomSession> {
        val name = RoomJoinInput.nickname(nickname) ?: return invalidInput()
        if (!RoomJoinInput.validCode(code)) return invalidInput()
        return acquire(Request.Join(code, name), accountGeneration)
    }

    private suspend fun acquire(
        request: Request,
        accountGeneration: Long? = null,
    ): RoomResult<RoomSession> = admission.withLock {
        when (val saved = coordinator.restore()) {
            is RoomResult.Failed -> return@withLock saved
            is RoomResult.Success -> if (saved.value != null) return@withLock alreadyInRoom()
        }
        val installation = try {
            store.installationId()
        } catch (_: IOException) {
            return@withLock storageFailure(StorageKind.WRITE)
        }
        val current = accounts.state().value as? SessionState.Authenticated
        if (accountGeneration != null && accountGeneration != current?.sessionGeneration) {
            return@withLock RoomResult.Failed(
                RoomFailure(AppFailure.OperationInterrupted, RoomFailureReason.ACCOUNT_CHANGED),
            )
        }
        val owner = if (request is Request.Create) current?.sessionGeneration else accountGeneration
        requestRoom(request, installation, owner)
    }

    private suspend fun requestRoom(request: Request, installation: String, owner: Long?): RoomResult<RoomSession> {
        val attempt = pending?.takeIf { it.request == request && it.owner == owner }
            ?: Attempt(request, owner, UUID.randomUUID()).also { pending = it }
        val response = when (request) {
            is Request.Create -> remote.create(request.entry, installation, attempt.key, owner)
            is Request.Join -> remote.join(JoinAdmission(request.code, request.name, attempt.key), installation, owner)
        }
        val result = when (response) {
            is RoomResult.Failed -> response
            is RoomResult.Success -> coordinator.persist(response.value)
        }
        if (result !is RoomResult.Failed || !result.failure.uncertain) pending = null
        return result
    }

    private fun invalidInput() = RoomResult.Failed(RoomFailure(AppFailure.OperationInterrupted))

    private fun alreadyInRoom() =
        RoomResult.Failed(RoomFailure(AppFailure.OperationInterrupted, RoomFailureReason.ALREADY_IN_ROOM))

    private sealed interface Request {
        data class Create(val entry: UUID) : Request
        data class Join(val code: String, val name: String) : Request
    }

    private class Attempt(val request: Request, val owner: Long?, val key: UUID)
}
