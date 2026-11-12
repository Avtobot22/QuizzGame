package com.quizzgame.shared.splash.data

import com.quizzgame.core.network.api.ApiCallResult
import com.quizzgame.shared.splash.domain.entity.CurrentUser

interface SplashRemoteDataSource {
    suspend fun launch(): ApiCallResult<Unit>
    suspend fun currentUser(): ApiCallResult<CurrentUser>
}
