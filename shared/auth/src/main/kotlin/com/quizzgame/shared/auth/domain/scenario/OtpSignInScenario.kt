package com.quizzgame.shared.auth.domain.scenario

import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.domain.entity.AuthResult
import com.quizzgame.shared.auth.domain.entity.AuthSurface
import com.quizzgame.shared.auth.domain.entity.otp.OtpSignInAttempt
import com.quizzgame.shared.auth.domain.usecase.BeginSignInUseCase
import com.quizzgame.shared.auth.domain.usecase.LoadAuthMethodsUseCase
import com.quizzgame.shared.auth.domain.usecase.RequestOtpUseCase
import com.quizzgame.shared.auth.domain.usecase.ResendOtpUseCase
import com.quizzgame.shared.auth.domain.usecase.VerifyOtpUseCase
import javax.inject.Inject

class OtpSignInScenario @Inject internal constructor(
    private val loadAuthMethodsUseCase: LoadAuthMethodsUseCase,
    private val beginSignInUseCase: BeginSignInUseCase,
    private val requestOtpUseCase: RequestOtpUseCase,
    private val resendOtpUseCase: ResendOtpUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase,
) {

    suspend fun methods(surface: AuthSurface): AuthResult<List<AuthMethod>> = loadAuthMethodsUseCase(surface).map {
        it.filterNot { method -> surface == AuthSurface.TV && method == AuthMethod.YANDEX }
    }

    suspend fun request(method: AuthMethod, identifier: String): AuthResult<OtpSignInAttempt> {
        val session = beginSignInUseCase() ?: return AuthResult.Failed(AuthFailure.SessionChanged)
        return requestOtpUseCase(method, identifier).map { OtpSignInAttempt(it, session) }
    }

    suspend fun resend(attempt: OtpSignInAttempt): AuthResult<OtpSignInAttempt> =
        resendOtpUseCase(attempt.challenge).map { OtpSignInAttempt(it, attempt.session) }

    suspend fun verify(attempt: OtpSignInAttempt, code: String): AuthResult<Unit> {
        val result = verifyOtpUseCase(attempt.session, attempt.challenge.id, code, attempt.keyFor(code))
        if ((result as? AuthResult.Failed)?.failure == AuthFailure.InvalidCode) attempt.forgetVerification()
        return result
    }
}
