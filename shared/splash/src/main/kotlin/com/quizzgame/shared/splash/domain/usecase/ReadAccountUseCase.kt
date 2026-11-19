package com.quizzgame.shared.splash.domain.usecase

import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.core.session.lifecycle.SessionStateReader
import com.quizzgame.shared.splash.domain.entity.AccountProfile
import com.quizzgame.shared.splash.domain.repository.CurrentUserReader
import com.quizzgame.shared.splash.domain.repository.SplashRepository
import javax.inject.Inject

class ReadAccountUseCase @Inject constructor(
    private val session: SessionStateReader,
    private val users: CurrentUserReader,
    private val startup: SplashRepository,
) {

    suspend operator fun invoke(): AccountProfile = when (val owner = session.current()) {
        SessionState.Guest            -> AccountProfile.Guest

        is SessionState.Authenticated -> {
            val user = users.current() ?: run {
                if (session.current() == owner) startup.loadUser(owner.sessionGeneration)
                users.current()
            }
            if (session.current() == owner) AccountProfile.SignedIn(user) else AccountProfile.Unavailable
        }

        else                          -> AccountProfile.Unavailable
    }
}
