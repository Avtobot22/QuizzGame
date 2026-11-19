package com.quizzgame.feature.auth.mobile.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzButton
import com.quizzgame.core.designsystem.mobile.QuizzButtonStyle
import com.quizzgame.core.designsystem.mobile.QuizzProviderButton
import com.quizzgame.core.ui.QuizzGroupDivider
import com.quizzgame.core.ui.QuizzIconLabel
import com.quizzgame.feature.auth.mobile.R
import com.quizzgame.feature.auth.mobile.ui.code.MobileAuthCode
import com.quizzgame.feature.auth.mobile.ui.form.MobileAuthIdentifier
import com.quizzgame.feature.auth.mobile.ui.loading.MobileAuthLoading
import com.quizzgame.feature.auth.mobile.ui.tags.MobileSignInTags
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.presentation.OtpIntent
import com.quizzgame.shared.auth.presentation.OtpState
import com.quizzgame.shared.auth.presentation.OtpStep
import com.quizzgame.core.ui.R as UiR

@Composable
internal fun MobileAuthStepContent(state: OtpState, elapsed: Long, onIntent: (OtpIntent) -> Unit) {
    when (val step = state.step) {
        OtpStep.Loading -> MobileAuthLoading()
        is OtpStep.Methods -> {
            MobileAuthError(state.failure)
            AuthMethods(step, !state.busy, onIntent)
        }
        is OtpStep.Identifier -> MobileAuthIdentifier(step, state, elapsed, onIntent)
        is OtpStep.Code -> MobileAuthCode(step, state, elapsed, onIntent)
        OtpStep.Finished -> Unit
    }
}

@Composable
private fun AuthMethods(step: OtpStep.Methods, enabled: Boolean, onIntent: (OtpIntent) -> Unit) {
    val (direct, providers) = step.choices.partition(AuthMethod::usesOtp)
    direct.forEachIndexed { index, method ->
        val email = method == AuthMethod.EMAIL
        val icon = if (email) UiR.drawable.ic_quizz_mail else UiR.drawable.ic_quizz_sms
        val label = if (email) R.string.auth_email else R.string.auth_sms
        val tag = if (email) MobileSignInTags.EMAIL else MobileSignInTags.SMS
        QuizzButton(
            { onIntent(OtpIntent.Choose(method)) },
            Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag(tag),
            style = if (index == 0) QuizzButtonStyle.FILLED else QuizzButtonStyle.OUTLINED,
            enabled = enabled,
        ) {
            QuizzIconLabel(icon, gap = 10.dp, tint = LocalContentColor.current) { Text(stringResource(label)) }
        }
    }
    if (direct.isNotEmpty() && providers.isNotEmpty()) {
        QuizzGroupDivider(MaterialTheme.colorScheme.outlineVariant)
    }
    providers.forEach { method ->
        val google = method == AuthMethod.GOOGLE
        val tag = if (google) MobileSignInTags.GOOGLE else MobileSignInTags.YANDEX
        QuizzProviderButton(
            text = stringResource(if (google) R.string.auth_google else R.string.auth_yandex),
            icon = if (google) UiR.drawable.ic_quizz_google_g else UiR.drawable.ic_quizz_yandex_id,
            onClick = { onIntent(OtpIntent.Choose(method)) },
            modifier = Modifier.fillMaxWidth().testTag(tag),
            enabled = enabled,
        )
    }
}
