package com.quizzgame.feature.auth.mobile.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.feature.auth.mobile.presentation.MobileSignInState
import com.quizzgame.feature.auth.mobile.presentation.MobileSignInStep
import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.domain.entity.otp.OtpChallenge
import com.quizzgame.shared.auth.presentation.OtpCodeInput
import java.time.Instant

internal class MobileSignInPreviewProvider : PreviewParameterProvider<MobileSignInState> {
    private val examples = listOf(
        "Загрузка способов" to MobileSignInState(),
        "Ошибка загрузки" to MobileSignInState(failure = AuthFailure.General(AppFailure.InvalidServerData)),
        "Нет способов входа" to MobileSignInState(MobileSignInStep.Methods(emptyList(), emptyList())),
        "Выбор способа" to MobileSignInState(
            MobileSignInStep.Methods(
                listOf(AuthMethod.EMAIL, AuthMethod.SMS),
                listOf(AuthMethod.GOOGLE, AuthMethod.YANDEX),
            ),
        ),
        "Ввод почты" to MobileSignInState(MobileSignInStep.Identifier(AuthMethod.EMAIL)),
        "Ввод телефона" to MobileSignInState(MobileSignInStep.Identifier(AuthMethod.SMS, "+7 912 345-67-89")),
        "Отправка SMS" to MobileSignInState(
            MobileSignInStep.Identifier(AuthMethod.SMS, "+1 202 555 0145"),
            busy = true,
        ),
        "Код с таймером" to MobileSignInState(sampleCode(input = "012")),
        "Проверка кода" to MobileSignInState(sampleCode(input = SAMPLE_CODE), busy = true),
        "Неверный код" to MobileSignInState(sampleCode(input = ""), failure = AuthFailure.InvalidCode),
        "Истёкший код" to MobileSignInState(
            sampleCode(input = SAMPLE_CODE, resendAt = 0),
            failure = AuthFailure.Expired,
        ),
        "Завершение" to MobileSignInState(MobileSignInStep.Finished),
    )

    override val values = examples.asSequence().map { it.second }

    override fun getDisplayName(index: Int) = examples.getOrNull(index)?.first

    private fun sampleCode(input: String, resendAt: Long = RESEND_AT): MobileSignInStep.Code = MobileSignInStep.Code(
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
