package com.quizzgame.feature.dashboard.mobile.data

import com.quizzgame.core.network.auth.CredentialUnavailableKind
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.feature.dashboard.mobile.domain.entity.Account
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AccountRepositoryTest {
    @Test
    fun blockedSessionNeverBecomesGuest() = runTest {
        val repository = AccountRepositoryImpl(
            { SessionState.Blocked(CredentialUnavailableKind.NOT_INITIALIZED) },
            { error("Blocked session must not read the profile") },
        )
        assertEquals(Account.Unavailable, repository.current())
    }

    @Test
    fun accountChangeDuringProfileReadPreventsPublication() = runTest {
        var owner: SessionState = SessionState.Authenticated(1)
        val repository = AccountRepositoryImpl({ owner }, {
            owner = SessionState.Authenticated(2)
            null
        })
        assertEquals(Account.Unavailable, repository.current())
    }
}
