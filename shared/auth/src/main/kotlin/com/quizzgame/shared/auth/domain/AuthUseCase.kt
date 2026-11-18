package com.quizzgame.shared.auth.domain

import com.quizzgame.core.session.lifecycle.SessionChangeResult
import com.quizzgame.core.session.model.TokenPair
import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.domain.entity.AuthResult
import com.quizzgame.shared.auth.domain.entity.AuthSurface
import com.quizzgame.shared.auth.domain.entity.OtpChallenge
import com.quizzgame.shared.auth.domain.entity.YandexAttempt
import javax.inject.Inject

/** Выполняет вход и публикует успех после записи пары QuizzGame в хранилище сессий. */
class AuthUseCase @Inject internal constructor(
    private val repository: AuthRepository,
    private val sessionAccess: AuthSessionAccess,
) {
    suspend fun methods(surface: AuthSurface) = repository.methods(surface)

    suspend fun begin(): AuthSessionAttempt? = sessionAccess.forSignIn()

    suspend fun request(method: AuthMethod, identifier: String): AuthResult<OtpChallenge> {
        val normalized = normalizeIdentifier(method, identifier)
            ?: return AuthResult.Failed(AuthFailure.InvalidIdentifier)
        return repository.request(method, normalized)
    }

    suspend fun resend(challenge: OtpChallenge) = repository.resend(challenge)

    suspend fun verify(attempt: AuthSessionAttempt, id: String, code: String, key: String): AuthResult<Unit> =
        commit(attempt, repository.verify(id, code, attempt.deviceId, key))

    suspend fun googleChallenge() = repository.googleChallenge()

    suspend fun verifyGoogle(attempt: AuthSessionAttempt, id: String, token: String): AuthResult<Unit> =
        commit(attempt, repository.verifyGoogle(id, token, attempt.deviceId))

    suspend fun yandexAttempt(attempt: AuthSessionAttempt) = repository.yandexAttempt(attempt.deviceId)

    suspend fun redeemYandex(attempt: AuthSessionAttempt, oauth: YandexAttempt) =
        commit(attempt, repository.redeemYandex(oauth, attempt.deviceId))

    private suspend fun commit(attempt: AuthSessionAttempt, result: AuthResult<TokenPair>): AuthResult<Unit> =
        when (result) {
            is AuthResult.Failed -> result
            is AuthResult.Success -> when (attempt.signIn(result.value)) {
                SessionChangeResult.Committed -> AuthResult.Success(Unit)
                is SessionChangeResult.NeedsStorageRecovery -> AuthResult.Failed(AuthFailure.StorageRecovery)
                SessionChangeResult.BusyOrBlocked, SessionChangeResult.Closed ->
                    AuthResult.Failed(AuthFailure.SessionChanged)
            }
        }
}
