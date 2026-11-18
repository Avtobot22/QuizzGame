package com.quizzgame.shared.auth.domain

import com.quizzgame.core.session.model.TokenPair
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.domain.entity.AuthResult
import com.quizzgame.shared.auth.domain.entity.AuthSurface
import com.quizzgame.shared.auth.domain.entity.GoogleChallenge
import com.quizzgame.shared.auth.domain.entity.OtpChallenge
import com.quizzgame.shared.auth.domain.entity.YandexAttempt

internal interface AuthRepository {
    suspend fun methods(surface: AuthSurface): AuthResult<List<AuthMethod>>
    suspend fun request(method: AuthMethod, identifier: String): AuthResult<OtpChallenge>
    suspend fun resend(challenge: OtpChallenge): AuthResult<OtpChallenge>
    suspend fun verify(id: String, code: String, device: String, key: String): AuthResult<TokenPair>
    suspend fun googleChallenge(): AuthResult<GoogleChallenge>
    suspend fun verifyGoogle(id: String, token: String, device: String): AuthResult<TokenPair>
    suspend fun yandexAttempt(device: String): AuthResult<YandexAttempt>
    suspend fun redeemYandex(attempt: YandexAttempt, device: String): AuthResult<TokenPair>
}
