package com.quizzgame.shared.account.presentation

sealed interface ProfileIntent {
    data object Visible : ProfileIntent
    data object Retry : ProfileIntent
    data object EditNickname : ProfileIntent
    data class ChangeNickname(val value: String) : ProfileIntent
    data object SuggestNickname : ProfileIntent
    data object CancelNickname : ProfileIntent
    data object SaveNickname : ProfileIntent
}
