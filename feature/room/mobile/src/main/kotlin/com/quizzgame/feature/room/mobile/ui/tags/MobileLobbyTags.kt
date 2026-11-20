package com.quizzgame.feature.room.mobile.ui.tags

object MobileLobbyTags {
    const val ROOT = "mobile_lobby"
    const val ROSTER = "mobile_lobby_roster"
    const val EXIT = "mobile_lobby_exit"
    const val READY = "mobile_lobby_ready"

    fun player(id: String): String = "mobile_lobby_player_$id"
}
