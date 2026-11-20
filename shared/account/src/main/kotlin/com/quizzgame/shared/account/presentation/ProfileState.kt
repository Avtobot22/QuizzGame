package com.quizzgame.shared.account.presentation

import com.quizzgame.shared.account.domain.entity.PlayerProfile

sealed interface ProfileState {
    data object Loading : ProfileState
    data class Ready(
        val profile: PlayerProfile,
        val refreshing: Boolean = false,
        val editor: NicknameEditorState? = null,
    ) : ProfileState
}
