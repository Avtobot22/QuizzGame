package com.quizzgame.shared.splash.domain

import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.core.session.lifecycle.SessionStateReader
import com.quizzgame.shared.splash.domain.entity.AccountProfile
import javax.inject.Inject

/** Читает профиль текущей сессии и при необходимости запрашивает его повторно. */
class ReadAccountUseCase @Inject constructor(
    private val session: SessionStateReader,
    private val users: CurrentUserReader,
    private val startup: SplashRepository,
) {
    suspend operator fun invoke(): AccountProfile = when (val owner = session.current()) {
        SessionState.Guest -> AccountProfile.Guest
        is SessionState.Authenticated -> {
            val user = users.current() ?: run {
                if (session.current() == owner) startup.loadUser(owner.sessionGeneration)
                users.current()
            }
            if (session.current() == owner) AccountProfile.SignedIn(user) else AccountProfile.Unavailable
        }
        else -> AccountProfile.Unavailable
    }
}
