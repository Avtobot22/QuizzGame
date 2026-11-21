package com.quizzgame.feature.room.mobile.presentation

internal sealed interface MobileLobbyIntent {
    data object Retry : MobileLobbyIntent
    sealed interface Command : MobileLobbyIntent

    data class SetReady(val ready: Boolean) : Command
    data object Leave : Command
}
