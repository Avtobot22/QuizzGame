package com.quizzgame.shared.splash.domain

import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.session.lifecycle.SessionState

interface SplashRepository {
    suspend fun currentSession(): SessionState
    suspend fun loadLaunch(): AppFailure?
    suspend fun refreshFlags(): AppFailure?
    suspend fun loadUser(expectedGeneration: Long): AppFailure?
    fun guestAllowed(): Boolean
    fun clearUser()
}
