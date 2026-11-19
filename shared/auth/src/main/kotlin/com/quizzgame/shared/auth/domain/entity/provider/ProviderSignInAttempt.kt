package com.quizzgame.shared.auth.domain.entity.provider

import com.quizzgame.core.session.lifecycle.SessionSignInAttempt

sealed interface ProviderSignInAttempt {
    class Google internal constructor(
        val challenge: GoogleChallenge,
        internal val session: SessionSignInAttempt,
    ) : ProviderSignInAttempt

    class Yandex internal constructor(
        val attempt: YandexAttempt,
        internal val session: SessionSignInAttempt,
    ) : ProviderSignInAttempt
}
