package com.quizzgame.shared.account.domain.entity

import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.shared.splash.domain.entity.CurrentUser

sealed interface SavePlayerNicknameResult {
    data class Saved(val user: CurrentUser) : SavePlayerNicknameResult
    data object InvalidName : SavePlayerNicknameResult
    data object AccountChanged : SavePlayerNicknameResult
    data class Failed(val cause: AppFailure) : SavePlayerNicknameResult
}
