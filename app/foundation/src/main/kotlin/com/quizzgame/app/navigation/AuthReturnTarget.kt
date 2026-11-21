package com.quizzgame.app.navigation

sealed interface AuthReturnTarget {
    data object Profile : AuthReturnTarget
    data object Home : AuthReturnTarget

    data class JoinRoomDraft(val code: String, val nickname: String) : AuthReturnTarget

    data class QuizEntry(val entryId: String) : AuthReturnTarget {
        init { require(ENTRY_ID.matches(entryId)) }
    }
}

private val ENTRY_ID = Regex("[A-Za-z0-9][A-Za-z0-9_-]{0,127}")
