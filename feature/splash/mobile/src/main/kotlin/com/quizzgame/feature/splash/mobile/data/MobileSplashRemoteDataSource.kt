package com.quizzgame.feature.splash.mobile.data

import com.quizzgame.core.network.api.ApiCallExecutor
import com.quizzgame.core.network.api.ApiCallResult
import com.quizzgame.core.network.auth.AuthPolicy
import com.quizzgame.core.network.auth.ReplayPolicy
import com.quizzgame.core.network.auth.RequestPolicy
import com.quizzgame.feature.splash.mobile.data.api.MobileSplashApi
import com.quizzgame.shared.splash.data.SplashRemoteDataSource
import com.quizzgame.shared.splash.data.mapper.toDomain
import com.quizzgame.shared.splash.data.mapper.validated
import com.quizzgame.shared.splash.domain.entity.CurrentUser
import javax.inject.Inject

internal class MobileSplashRemoteDataSource @Inject constructor(
    private val executor: ApiCallExecutor,
) : SplashRemoteDataSource {

    override suspend fun launch(): ApiCallResult<Unit> = executor.call(MobileSplashApi::class.java) {
        launch(RequestPolicy(AuthPolicy.OPTIONAL, ReplayPolicy.AFTER_REFRESH_ONCE)).validated()
    }

    override suspend fun currentUser(): ApiCallResult<CurrentUser> = executor.call(MobileSplashApi::class.java) {
        currentUser(RequestPolicy(AuthPolicy.REQUIRED, ReplayPolicy.AFTER_REFRESH_ONCE)).toDomain()
    }
}
