package com.quizzgame.feature.auth.mobile.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.domain.entity.otp.OtpChallenge
import com.quizzgame.shared.auth.presentation.OtpState
import com.quizzgame.shared.auth.presentation.OtpStep
import java.time.Instant

internal class MobileSignInPreviewProvider : PreviewParameterProvider<OtpState> {
    private val examples = listOf(
        "Загрузка способов" to OtpState(),
        "Ошибка загрузки" to OtpState(failure = AuthFailure.General(AppFailure.InvalidServerData)),
        "Нет способов входа" to OtpState(OtpStep.Methods(emptyList())),
        "Выбор способа" to OtpState(
            OtpStep.Methods(listOf(AuthMethod.EMAIL, AuthMethod.SMS, AuthMethod.GOOGLE, AuthMethod.YANDEX)),
        ),
        "Ввод почты" to OtpState(OtpStep.Identifier(AuthMethod.EMAIL)),
        "Ввод телефона" to OtpState(OtpStep.Identifier(AuthMethod.SMS, "+7 912 345-67-89")),
        "Отправка SMS" to OtpState(OtpStep.Identifier(AuthMethod.SMS, "+1 202 555 0145"), busy = true),
        "Код с таймером" to OtpState(sampleCode(input = "012")),
        "Проверка кода" to OtpState(sampleCode(input = SAMPLE_CODE), busy = true),
        "Неверный код" to OtpState(sampleCode(input = ""), failure = AuthFailure.InvalidCode),
        "Истёкший код" to OtpState(sampleCode(input = SAMPLE_CODE, resendAt = 0), failure = AuthFailure.Expired),
        "Завершение" to OtpState(OtpStep.Finished),
    )

    override val values = examples.asSequence().map { it.second }

    override fun getDisplayName(index: Int) = examples.getOrNull(index)?.first

    private fun sampleCode(input: String, resendAt: Long = RESEND_AT): OtpStep.Code = OtpStep.Code(
        AuthMethod.EMAIL,
        "guest@example.test",
        OtpChallenge("preview", "g***@example.test", SAMPLE_CODE.length, Instant.EPOCH, 0, null),
        EXPIRES_AT,
        resendAt,
        input,
    )

    private companion object {
        const val RESEND_AT = 24_000L
        const val EXPIRES_AT = 120_000L
        const val SAMPLE_CODE = "012345"
    }
}
