package com.quizzgame.feature.room.mobile.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.error.failure.ConnectivityKind
import com.quizzgame.feature.room.mobile.presentation.MobileLobbyGameInfo
import com.quizzgame.feature.room.mobile.presentation.MobileLobbyIntent
import com.quizzgame.feature.room.mobile.presentation.MobileLobbyState
import com.quizzgame.shared.room.domain.entity.result.RoomFailure
import com.quizzgame.shared.room.domain.entity.result.RoomFailureReason
import com.quizzgame.shared.room.domain.entity.session.ParticipantId
import com.quizzgame.shared.room.domain.entity.session.RoomId
import com.quizzgame.shared.room.domain.entity.session.RoomStreamState
import com.quizzgame.shared.room.domain.entity.snapshot.RoomCapacity
import com.quizzgame.shared.room.domain.entity.snapshot.RoomConnection
import com.quizzgame.shared.room.domain.entity.snapshot.RoomEntry
import com.quizzgame.shared.room.domain.entity.snapshot.RoomParticipant
import com.quizzgame.shared.room.domain.entity.snapshot.RoomPermissions
import com.quizzgame.shared.room.domain.entity.snapshot.RoomPhase
import com.quizzgame.shared.room.domain.entity.snapshot.RoomPolicy
import com.quizzgame.shared.room.domain.entity.snapshot.RoomSnapshot
import com.quizzgame.shared.room.presentation.RoomLobbyCommandError
import java.time.Instant
import java.util.UUID

internal class MobileLobbyPreviewProvider : PreviewParameterProvider<MobileLobbyState> {
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
        RoomPermissions(canStart = false, canClose = false, canKick = false, canReady = true),
        expiry,
        null,
    )
    private val content = MobileLobbyState(
        RoomStreamState.Active(room),
        self = maya.id,
        gameInfo = MobileLobbyGameInfo("Вопросы о кино, науке и путешествиях.", 12, 600),
    )
    private val leaveError = RoomLobbyCommandError(
        MobileLobbyIntent.Leave,
        RoomFailure(AppFailure.Connectivity(ConnectivityKind.TIMEOUT), uncertain = true),
    )
    override val values = sequenceOf(
        MobileLobbyState(),
        content,
        content.copy(stream = RoomStreamState.Active(room.copy(players = emptyList()))),
        content.copy(stream = RoomStreamState.Reconnecting(room)),
        content.copy(stream = RoomStreamState.Active(room.copy(phase = RoomPhase.STARTING))),
        MobileLobbyState(RoomStreamState.Ended(roomId, RoomFailureReason.CLOSED)),
        MobileLobbyState(RoomStreamState.Failed(RoomFailure(AppFailure.InvalidServerData))),
        MobileLobbyState(RoomStreamState.Reconnecting(last = null)),
        content.copy(
            commandError = RoomLobbyCommandError(
                MobileLobbyIntent.SetReady(false),
                RoomFailure(AppFailure.InvalidServerData),
            ),
        ),
        MobileLobbyState(RoomStreamState.Started(UUID(0, 5)), self = maya.id),
        content.copy(gameInfo = null),
        content.copy(stream = RoomStreamState.Active(room.copy(phase = RoomPhase.IN_GAME))),
        content.copy(stream = RoomStreamState.Reconnecting(room), commandError = leaveError),
        MobileLobbyState(RoomStreamState.Started(UUID(0, 5)), self = maya.id, commandError = leaveError),
    )
}
