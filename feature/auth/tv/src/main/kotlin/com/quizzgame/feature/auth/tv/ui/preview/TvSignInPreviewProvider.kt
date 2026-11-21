package com.quizzgame.feature.auth.tv.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.feature.auth.tv.presentation.TvSignInState
import com.quizzgame.feature.auth.tv.presentation.TvSignInStep
import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.domain.entity.otp.OtpChallenge
import com.quizzgame.shared.auth.presentation.OtpCodeInput
import java.time.Instant

internal class TvSignInPreviewProvider : PreviewParameterProvider<TvSignInState> {
    private val methods = TvSignInState(
        TvSignInStep.Methods(listOf(AuthMethod.EMAIL, AuthMethod.SMS), google = true),
    )
    private val examples = listOf(
        "Загрузка способов" to TvSignInState(),
        "Ошибка загрузки" to TvSignInState(failure = AuthFailure.General(AppFailure.InvalidServerData)),
        "Нет способов входа" to TvSignInState(TvSignInStep.Methods(emptyList(), google = false)),
        "Выбор способа" to methods,
        "Только Google" to TvSignInState(TvSignInStep.Methods(emptyList(), google = true)),
        "Ввод почты" to TvSignInState(TvSignInStep.Identifier(AuthMethod.EMAIL)),
        "Ввод телефона" to TvSignInState(TvSignInStep.Identifier(AuthMethod.SMS, "+7 912 345-67-89")),
        "Отправка SMS" to TvSignInState(TvSignInStep.Identifier(AuthMethod.SMS, "+1 202 555 0145"), busy = true),
        "Ввод кода" to TvSignInState(sampleCode(input = "012")),
        "Проверка кода" to TvSignInState(sampleCode(input = SAMPLE_CODE), busy = true),
        "Неверный код" to TvSignInState(sampleCode(input = ""), failure = AuthFailure.InvalidCode),
        "Истёкший код" to TvSignInState(sampleCode(input = "", resendAt = 0), failure = AuthFailure.Expired),
        "Завершение" to TvSignInState(TvSignInStep.Finished),
    )

    override val values = examples.asSequence().map { it.second }

    override fun getDisplayName(index: Int) = examples.getOrNull(index)?.first

    private fun sampleCode(input: String, resendAt: Long = RESEND_AT): TvSignInStep.Code = TvSignInStep.Code(
        AuthMethod.EMAIL,
        "guest@example.test",
        OtpCodeInput(
            OtpChallenge("preview", "g***@example.test", SAMPLE_CODE.length, Instant.EPOCH, 0, null),
            EXPIRES_AT,
            resendAt,
            input,
        ),
    )

    private companion object {
        const val RESEND_AT = 24_000L
        const val EXPIRES_AT = 120_000L
        const val SAMPLE_CODE = "012345"
    }
}
