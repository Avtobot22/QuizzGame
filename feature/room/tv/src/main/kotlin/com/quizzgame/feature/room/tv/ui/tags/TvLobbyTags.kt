package com.quizzgame.feature.room.tv.ui.tags

object TvLobbyTags {
    const val ROOT = "tv_lobby"
    const val ROSTER = "tv_lobby_roster"
    const val EXIT = "tv_lobby_exit"
    const val RETRY = "tv_lobby_retry"
    const val CODE = "tv_lobby_code"

    fun player(id: String): String = "tv_lobby_player_$id"
}
