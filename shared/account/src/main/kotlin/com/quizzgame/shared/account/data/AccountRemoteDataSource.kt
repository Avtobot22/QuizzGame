package com.quizzgame.shared.account.data

import com.quizzgame.core.network.api.ApiCallResult
import com.quizzgame.core.network.auth.AuthPolicy
import com.quizzgame.core.network.auth.ReplayPolicy
import com.quizzgame.core.network.auth.RequestPolicy
import com.quizzgame.shared.splash.domain.entity.CurrentUser

interface AccountRemoteDataSource {
    suspend fun loadProfile(generation: Long): ApiCallResult<CurrentUser>
    suspend fun updateNickname(name: String, generation: Long): ApiCallResult<CurrentUser>
}

fun nicknameUpdatePolicy(generation: Long) =
    RequestPolicy(AuthPolicy.REQUIRED, requiredSessionGeneration = generation)

fun profileReadPolicy(generation: Long) =
    RequestPolicy(AuthPolicy.REQUIRED, ReplayPolicy.AFTER_REFRESH_ONCE, requiredSessionGeneration = generation)
