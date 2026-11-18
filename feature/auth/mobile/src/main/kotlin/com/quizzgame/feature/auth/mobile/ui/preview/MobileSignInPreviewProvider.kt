package com.quizzgame.feature.auth.mobile.ui.preview

import android.os.SystemClock
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.domain.entity.OtpChallenge
import com.quizzgame.shared.auth.domain.entity.OtpState
import com.quizzgame.shared.auth.domain.entity.OtpStep
import java.time.Instant

internal class MobileSignInPreviewProvider : PreviewParameterProvider<OtpState> {
    override val values = sequenceOf(
        OtpState(busy = true),
        OtpState(OtpStep.Methods(listOf(AuthMethod.EMAIL, AuthMethod.SMS, AuthMethod.GOOGLE, AuthMethod.YANDEX))),
        OtpState(OtpStep.Identifier(AuthMethod.EMAIL)),
        OtpState(activeCode()),
        OtpState(activeCode(), failure = AuthFailure.InvalidCode),
        OtpState(activeCode(), failure = AuthFailure.Expired),
    )

    private fun activeCode(): OtpStep.Code {
        val elapsed = SystemClock.elapsedRealtime()
        return OtpStep.Code(
            AuthMethod.EMAIL,
            "guest@example.test",
            OtpChallenge("preview", "g***@example.test", SAMPLE_CODE.length, Instant.EPOCH, 0, null),
            elapsed + DISPLAY_TIME_MILLIS,
            elapsed,
            SAMPLE_CODE,
        )
    }

    private companion object {
        const val DISPLAY_TIME_MILLIS = 60_000L
        const val SAMPLE_CODE = "012345"
    }
}
