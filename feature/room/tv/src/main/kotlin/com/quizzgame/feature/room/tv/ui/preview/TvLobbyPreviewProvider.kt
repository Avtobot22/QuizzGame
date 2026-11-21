package com.quizzgame.feature.room.tv.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.shared.room.domain.entity.result.RoomFailure
import com.quizzgame.shared.room.domain.entity.result.RoomFailureReason
import com.quizzgame.shared.room.domain.entity.session.ParticipantId
import com.quizzgame.shared.room.domain.entity.session.RoomId
import com.quizzgame.shared.room.domain.entity.session.RoomStreamState
import com.quizzgame.shared.room.domain.entity.snapshot.RoomCapacity
import com.quizzgame.shared.room.domain.entity.snapshot.RoomConnection
import com.quizzgame.shared.room.domain.entity.snapshot.RoomEntry
import com.quizzgame.shared.room.domain.entity.snapshot.RoomInvite
import com.quizzgame.shared.room.domain.entity.snapshot.RoomParticipant
import com.quizzgame.shared.room.domain.entity.snapshot.RoomPermissions
import com.quizzgame.shared.room.domain.entity.snapshot.RoomPhase
import com.quizzgame.shared.room.domain.entity.snapshot.RoomPolicy
import com.quizzgame.shared.room.domain.entity.snapshot.RoomSnapshot
import com.quizzgame.shared.room.presentation.RoomLobbyState
import java.time.Instant
import java.util.UUID

internal class TvLobbyPreviewProvider : PreviewParameterProvider<RoomLobbyState> {
    private val expiry = Instant.parse("2027-01-01T18:00:00Z")
    private val roomId = RoomId(UUID(0, 1))
    private val maya = RoomParticipant(ParticipantId(UUID(0, 2)), "Майя", true, RoomConnection.CONNECTED)
    private val lev = RoomParticipant(ParticipantId(UUID(0, 3)), "Лев", false, RoomConnection.RECONNECTING)
    private val room = RoomSnapshot(
        roomId,
        RoomEntry(UUID(0, 4), "Обо всём на свете"),
        RoomPhase.LOBBY,
        3,
        RoomCapacity(2, 16),
        RoomPolicy(guestPlayersAllowed = true, allPlayersReadyRequired = true),
        listOf(maya, lev),
        RoomPermissions(canStart = false, canClose = true, canKick = true, canReady = false),
        expiry,
        RoomInvite("031482", "https://join.example.test/r/031482", expiry),
    )
    private val content = RoomLobbyState(RoomStreamState.Active(room), self = null)
    override val values = sequenceOf(
        RoomLobbyState(),
        content,
        content.copy(stream = RoomStreamState.Active(room.copy(players = emptyList()))),
        content.copy(stream = RoomStreamState.Reconnecting(room)),
        content.copy(stream = RoomStreamState.Active(room.copy(phase = RoomPhase.STARTING))),
        RoomLobbyState(RoomStreamState.Ended(roomId, RoomFailureReason.CLOSED)),
        RoomLobbyState(RoomStreamState.Failed(RoomFailure(AppFailure.InvalidServerData))),
        RoomLobbyState(RoomStreamState.Reconnecting(last = null)),
        content.copy(error = RoomFailure(AppFailure.InvalidServerData)),
        RoomLobbyState(RoomStreamState.Started(UUID(0, 5))),
        content.copy(stream = RoomStreamState.Active(room.copy(phase = RoomPhase.IN_GAME))),
    )
}
