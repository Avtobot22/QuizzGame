package com.quizzgame.feature.room.tv.presentation

import com.quizzgame.shared.room.domain.entity.session.ParticipantId

internal sealed interface TvLobbyIntent {
    data object Retry : TvLobbyIntent
    sealed interface Command : TvLobbyIntent

    data class Kick(val participantId: ParticipantId) : Command
    data object Close : Command
}
