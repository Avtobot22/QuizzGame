package com.quizzgame.feature.dashboard.mobile.data

import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.network.auth.CredentialUnavailableKind
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.feature.dashboard.mobile.domain.entity.Account
import com.quizzgame.shared.splash.domain.ReadAccountUseCase
import com.quizzgame.shared.splash.domain.SplashRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AccountRepositoryTest {
    private val unusedStartup = object : SplashRepository {
        override suspend fun currentSession(): SessionState = error("Unexpected startup")
        override suspend fun loadLaunch(): AppFailure? = error("Unexpected startup")
        override suspend fun refreshFlags(): AppFailure? = error("Unexpected flags")
        override suspend fun loadUser(expectedGeneration: Long): AppFailure? = error("Unexpected read")
        override fun guestAllowed(): Boolean = error("Unexpected flags")
        override fun clearUser() = Unit
    }

    @Test
    fun blockedSessionNeverBecomesGuest() = runTest {
        val repository = AccountRepositoryImpl(
            ReadAccountUseCase(
                { SessionState.Blocked(CredentialUnavailableKind.NOT_INITIALIZED) },
                { error("Blocked session must not read the profile") },
                unusedStartup,
            ),
        )
        assertEquals(Account.Unavailable, repository.current())
    }

    @Test
    fun accountChangeDuringProfileReadPreventsPublication() = runTest {
        var owner: SessionState = SessionState.Authenticated(1)
        val repository = AccountRepositoryImpl(
            ReadAccountUseCase({ owner }, {
                owner = SessionState.Authenticated(2)
                null
            }, unusedStartup),
        )
        assertEquals(Account.Unavailable, repository.current())
    }
}
