package com.quizzgame.feature.dashboard.mobile.data

import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.core.session.lifecycle.SessionStateReader
import com.quizzgame.feature.dashboard.mobile.domain.AccountRepository
import com.quizzgame.feature.dashboard.mobile.domain.entity.Account
import com.quizzgame.shared.splash.domain.CurrentUserReader
import javax.inject.Inject

internal class AccountRepositoryImpl @Inject constructor(
    private val session: SessionStateReader,
    private val users: CurrentUserReader,
) : AccountRepository {
    override suspend fun current(): Account {
        val generation = session.current()
        return when (generation) {
            SessionState.Guest, SessionState.Expired -> Account.Guest
            is SessionState.Authenticated -> {
                val user = users.current()
                if (session.current() == generation) Account.SignedIn(user?.displayName) else Account.Unavailable
            }
            else -> Account.Unavailable
        }
    }
}
