package com.quizzgame.shared.auth.domain.scenario

import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.domain.entity.AuthResult
import com.quizzgame.shared.auth.domain.entity.provider.ProviderSignInAttempt
import com.quizzgame.shared.auth.domain.usecase.BeginSignInUseCase
import com.quizzgame.shared.auth.domain.usecase.CreateGoogleChallengeUseCase
import com.quizzgame.shared.auth.domain.usecase.CreateYandexAttemptUseCase
import com.quizzgame.shared.auth.domain.usecase.RedeemYandexAttemptUseCase
import com.quizzgame.shared.auth.domain.usecase.VerifyGoogleCredentialUseCase
import javax.inject.Inject

class ProviderSignInScenario @Inject internal constructor(
    private val beginSignInUseCase: BeginSignInUseCase,
    private val createGoogleChallengeUseCase: CreateGoogleChallengeUseCase,
    private val createYandexAttemptUseCase: CreateYandexAttemptUseCase,
    private val verifyGoogleCredentialUseCase: VerifyGoogleCredentialUseCase,
    private val redeemYandexAttemptUseCase: RedeemYandexAttemptUseCase,
) {
    suspend fun start(method: AuthMethod): AuthResult<ProviderSignInAttempt> {
        val session = beginSignInUseCase() ?: return AuthResult.Failed(AuthFailure.SessionChanged)
        return when (method) {
            AuthMethod.GOOGLE -> createGoogleChallengeUseCase().map { ProviderSignInAttempt.Google(it, session) }
            AuthMethod.YANDEX -> createYandexAttemptUseCase(session).map { ProviderSignInAttempt.Yandex(it, session) }
            AuthMethod.EMAIL, AuthMethod.SMS -> AuthResult.Failed(AuthFailure.ProviderRejected)
        }
    }

    suspend fun finishGoogle(attempt: ProviderSignInAttempt.Google, token: String): AuthResult<Unit> =
        verifyGoogleCredentialUseCase(attempt.session, attempt.challenge, token)

    suspend fun finishYandex(attempt: ProviderSignInAttempt.Yandex): AuthResult<Unit> =
        redeemYandexAttemptUseCase(attempt.session, attempt.attempt)
}
