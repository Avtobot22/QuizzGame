package com.quizzgame.feature.dashboard.mobile.data

import com.quizzgame.feature.dashboard.mobile.domain.entity.Account
import com.quizzgame.feature.dashboard.mobile.domain.repository.AccountRepository
import com.quizzgame.shared.splash.domain.entity.AccountProfile
import com.quizzgame.shared.splash.domain.usecase.ReadAccountUseCase
import javax.inject.Inject

internal class AccountRepositoryImpl @Inject constructor(
    private val readAccountUseCase: ReadAccountUseCase,
) : AccountRepository {
    override suspend fun current(): Account = when (val account = readAccountUseCase()) {
        AccountProfile.Guest -> Account.Guest
        AccountProfile.Unavailable -> Account.Unavailable
        is AccountProfile.SignedIn -> Account.SignedIn(account.user?.displayName, account.user == null)
    }
}
