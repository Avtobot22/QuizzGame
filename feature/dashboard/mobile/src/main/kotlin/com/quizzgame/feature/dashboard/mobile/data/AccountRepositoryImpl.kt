package com.quizzgame.feature.dashboard.mobile.data

import com.quizzgame.feature.dashboard.mobile.domain.AccountRepository
import com.quizzgame.feature.dashboard.mobile.domain.entity.Account
import com.quizzgame.shared.splash.domain.ReadAccountUseCase
import com.quizzgame.shared.splash.domain.entity.AccountProfile
import javax.inject.Inject

internal class AccountRepositoryImpl @Inject constructor(private val profile: ReadAccountUseCase) : AccountRepository {
    override suspend fun current(): Account = when (val account = profile()) {
        AccountProfile.Guest -> Account.Guest
        AccountProfile.Unavailable -> Account.Unavailable
        is AccountProfile.SignedIn -> Account.SignedIn(account.user?.displayName, account.user == null)
    }
}
