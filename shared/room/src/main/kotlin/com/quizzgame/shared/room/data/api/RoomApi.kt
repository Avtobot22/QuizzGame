package com.quizzgame.shared.room.data.api

import com.quizzgame.core.network.auth.RequestPolicy
import com.quizzgame.shared.room.data.dto.admission.CreateRoomRequest
import com.quizzgame.shared.room.data.dto.admission.CreateRoomResponse
import com.quizzgame.shared.room.data.dto.admission.JoinRoomRequest
import com.quizzgame.shared.room.data.dto.admission.JoinRoomResponse
import com.quizzgame.shared.room.data.dto.command.ReadyRequest
import com.quizzgame.shared.room.data.dto.command.StartRequest
import com.quizzgame.shared.room.data.dto.command.StartResponse
import com.quizzgame.shared.room.data.dto.snapshot.SnapshotDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Tag

internal const val ROOM_AUTH_HEADER = "X-Room-Token"
internal const val ROOM_ATTEMPT_HEADER = "Idempotency-Key"
internal const val ROOM_API = "/api/rooms"

internal interface RoomApi {
    @POST(ROOM_API)
    suspend fun create(
        @Body body: CreateRoomRequest,
        @Header(ROOM_ATTEMPT_HEADER) key: String,
        @Tag policy: RequestPolicy,
    ): CreateRoomResponse

    @POST("$ROOM_API/join")
    suspend fun join(
        @Body body: JoinRoomRequest,
        @Header(ROOM_ATTEMPT_HEADER) key: String,
        @Tag policy: RequestPolicy,
    ): JoinRoomResponse

    @GET("$ROOM_API/{roomId}")
    suspend fun snapshot(
        @Path("roomId") id: String,
        @Header(ROOM_AUTH_HEADER) token: String,
        @Tag policy: RequestPolicy,
    ): SnapshotDto

    @PUT("$ROOM_API/{roomId}/participants/me/ready")
    suspend fun ready(
        @Path("roomId") id: String,
        @Header(ROOM_AUTH_HEADER) token: String,
        @Body body: ReadyRequest,
        @Tag policy: RequestPolicy,
    ): SnapshotDto

    @POST("$ROOM_API/{roomId}/start")
    suspend fun start(
        @Path("roomId") id: String,
        @Header(ROOM_AUTH_HEADER) token: String,
        @Header(ROOM_ATTEMPT_HEADER) key: String,
        @Body body: StartRequest,
        @Tag policy: RequestPolicy,
    ): StartResponse

    @DELETE("$ROOM_API/{roomId}")
    suspend fun close(
        @Path("roomId") id: String,
        @Header(ROOM_AUTH_HEADER) token: String,
        @Tag policy: RequestPolicy,
    )

    @DELETE("$ROOM_API/{roomId}/participants/me")
    suspend fun leave(
        @Path("roomId") id: String,
        @Header(ROOM_AUTH_HEADER) token: String,
        @Tag policy: RequestPolicy,
    )

    @DELETE("$ROOM_API/{roomId}/participants/{participantId}")
    suspend fun kick(
        @Path("roomId") id: String,
        @Path("participantId") participant: String,
        @Header(ROOM_AUTH_HEADER) token: String,
        @Tag policy: RequestPolicy,
    )
}
