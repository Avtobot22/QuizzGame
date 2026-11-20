package com.quizzgame.shared.room.domain.entity.session

/** Состояние комнаты вместе с участником текущего устройства. */
data class RoomLobby(val stream: RoomStreamState, val self: ParticipantId?)
