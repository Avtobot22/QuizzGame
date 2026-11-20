package com.quizzgame.shared.room.data.api

import com.quizzgame.core.network.api.ApiCallExecutor
import com.quizzgame.core.network.auth.AuthPolicy
import com.quizzgame.core.network.auth.RequestPolicy
import com.quizzgame.shared.room.data.dto.admission.CreateRoomRequest
import com.quizzgame.shared.room.data.dto.admission.JoinRoomRequest
import com.quizzgame.shared.room.data.dto.command.ReadyRequest
import com.quizzgame.shared.room.data.dto.command.StartRequest
import com.quizzgame.shared.room.data.dto.snapshot.SnapshotDto
import com.quizzgame.shared.room.data.mapper.mapRoom
import com.quizzgame.shared.room.data.mapper.toDomain
import com.quizzgame.shared.room.data.storage.StoredRoom
import com.quizzgame.shared.room.domain.entity.admission.RoomJoinLink
import com.quizzgame.shared.room.domain.entity.command.RoomCommand
import com.quizzgame.shared.room.domain.entity.result.RoomResult
import com.quizzgame.shared.room.domain.entity.session.RoomRole
import com.quizzgame.shared.room.domain.entity.snapshot.RoomSnapshot
import java.util.UUID
import javax.inject.Inject

internal class RoomRemoteDataSource @Inject constructor(
    private val executor: ApiCallExecutor,
    private val links: RoomJoinLink,
) {
    suspend fun create(entry: UUID, installation: String, key: UUID, owner: Long?): RoomResult<AcquiredRoom> =
        executor.call(RoomApi::class.java) {
            create(CreateRoomRequest(entry.toString(), installation), key.toString(), admissionPolicy(owner))
        }.mapRoom {
            val snapshot = it.room.toDomain(RoomRole.HOST, links)
            val credential = it.credential.toDomain()
            require(snapshot.entry.id == entry && snapshot.join != null && credential.role == RoomRole.HOST)
            AcquiredRoom(StoredRoom(snapshot.roomId, credential, null, owner), snapshot)
        }

    suspend fun join(admission: JoinAdmission, installation: String, owner: Long?): RoomResult<AcquiredRoom> =
        executor.call(RoomApi::class.java) {
            val body = JoinRoomRequest(admission.code, admission.name, installation)
            join(body, admission.key.toString(), admissionPolicy(owner))
        }.mapRoom {
            val snapshot = it.room.toDomain(RoomRole.PARTICIPANT, links)
            val credential = it.credential.toDomain()
            val participant = it.participant.toDomain()
            require(credential.role == RoomRole.PARTICIPANT && participant in snapshot.players)
            AcquiredRoom(StoredRoom(snapshot.roomId, credential, participant.id, owner), snapshot)
        }

    suspend fun snapshot(room: StoredRoom): RoomResult<RoomSnapshot> = executor.call(RoomApi::class.java) {
        snapshot(room.id.toString(), room.credential.token, roomPolicy)
    }.mapRoom { it.forRoom(room) }

    suspend fun ready(room: StoredRoom, value: Boolean): RoomResult<RoomSnapshot> = executor.call(RoomApi::class.java) {
        ready(room.id.toString(), room.credential.token, ReadyRequest(value), roomPolicy)
    }.mapRoom { it.forRoom(room) }

    suspend fun start(room: StoredRoom, command: RoomCommand.Start): RoomResult<Long> =
        executor.call(RoomApi::class.java) {
            start(
                room.id.toString(),
                room.credential.token,
                command.key.toString(),
                StartRequest(command.expectedVersion),
                roomPolicy,
            )
        }.mapRoom {
            require(it.roomId == room.id.toString() && it.state == "starting" && it.version > command.expectedVersion)
            it.version
        }

    suspend fun remove(room: StoredRoom, command: RoomCommand): RoomResult<Unit> =
        executor.call(RoomApi::class.java) {
            val id = room.id.toString()
            val token = room.credential.token
            when (command) {
                RoomCommand.Close -> close(id, token, roomPolicy)
                RoomCommand.Leave -> leave(id, token, roomPolicy)
                is RoomCommand.Kick -> kick(id, command.participantId.toString(), token, roomPolicy)
                else -> error("Expected a room removal command")
            }
        }.mapRoom { }

    private fun SnapshotDto.forRoom(room: StoredRoom): RoomSnapshot =
        toDomain(room.credential.role, links).also { require(it.roomId == room.id) }
}

internal class AcquiredRoom(val room: StoredRoom, val snapshot: RoomSnapshot)
internal class JoinAdmission(val code: String, val name: String, val key: UUID)

private val roomPolicy = RequestPolicy()

private fun admissionPolicy(owner: Long?): RequestPolicy =
    if (owner == null) roomPolicy else RequestPolicy(AuthPolicy.REQUIRED, requiredSessionGeneration = owner)
