package com.quizzgame.feature.account.mobile.data

import com.quizzgame.core.network.api.ApiCallExecutor
import com.quizzgame.feature.account.mobile.data.api.MobileAccountApi
import com.quizzgame.shared.account.data.AccountRemoteDataSource
import com.quizzgame.shared.account.data.dto.UpdateNicknameRequest
import com.quizzgame.shared.account.data.nicknameUpdatePolicy
import com.quizzgame.shared.account.data.profileReadPolicy
import com.quizzgame.shared.splash.data.mapper.toDomain
import javax.inject.Inject

internal class MobileAccountRemoteDataSource @Inject constructor(
    private val executor: ApiCallExecutor,
) : AccountRemoteDataSource {
    override suspend fun loadProfile(generation: Long) = executor.call(MobileAccountApi::class.java) {
        profile(profileReadPolicy(generation)).toDomain()
    }

    override suspend fun updateNickname(name: String, generation: Long) = executor.call(MobileAccountApi::class.java) {
        updateNickname(UpdateNicknameRequest(name), nicknameUpdatePolicy(generation)).toDomain()
    }
}
