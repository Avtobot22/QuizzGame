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
import com.quizzgame.core.ui.tokens.QuizzIdentityDimensions
import com.quizzgame.feature.auth.mobile.R
import com.quizzgame.feature.auth.mobile.presentation.MobileSignInIntent
import com.quizzgame.feature.auth.mobile.presentation.MobileSignInState
import com.quizzgame.feature.auth.mobile.presentation.MobileSignInStep
import com.quizzgame.feature.auth.mobile.ui.code.MobileAuthCode
import com.quizzgame.feature.auth.mobile.ui.form.MobileAuthIdentifier
import com.quizzgame.feature.auth.mobile.ui.loading.MobileAuthLoading
import com.quizzgame.feature.auth.mobile.ui.tags.MobileSignInTags
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.core.ui.R as UiR

@Composable
internal fun MobileAuthStepContent(
    state: MobileSignInState,
    elapsed: Long,
    onIntent: (MobileSignInIntent) -> Unit,
    onProvider: (AuthMethod) -> Unit,
) {
    when (val step = state.step) {
        MobileSignInStep.Loading -> MobileAuthLoading()
        is MobileSignInStep.Methods -> {
            MobileAuthError(state.failure)
            AuthMethods(step, !state.busy, { onIntent(MobileSignInIntent.ChooseOtp(it)) }, onProvider)
        }
        is MobileSignInStep.Identifier -> MobileAuthIdentifier(step, state, elapsed, onIntent)
        is MobileSignInStep.Code -> MobileAuthCode(step, state, elapsed, onIntent)
        MobileSignInStep.Finished -> Unit
    }
}

@Composable
private fun AuthMethods(
    step: MobileSignInStep.Methods,
    enabled: Boolean,
    onOtp: (AuthMethod) -> Unit,
    onProvider: (AuthMethod) -> Unit,
) {
    step.otp.forEachIndexed { index, method ->
        val email = method == AuthMethod.EMAIL
        val icon = if (email) UiR.drawable.ic_quizz_mail else UiR.drawable.ic_quizz_sms
        val label = if (email) R.string.auth_email else R.string.auth_sms
        val tag = if (email) MobileSignInTags.EMAIL else MobileSignInTags.SMS
        QuizzButton(
            { onOtp(method) },
            Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag(tag),
            style = if (index == 0) QuizzButtonStyle.FILLED else QuizzButtonStyle.OUTLINED,
            enabled = enabled,
        ) {
            QuizzIconLabel(
                icon,
                iconSize = QuizzIdentityDimensions.actionIcon.dp,
                gap = 10.dp,
                tint = LocalContentColor.current,
            ) {
                Text(stringResource(label))
            }
        }
    }
    if (step.otp.isNotEmpty() && step.providers.isNotEmpty()) {
        QuizzGroupDivider(MaterialTheme.colorScheme.outlineVariant)
    }
    step.providers.forEach { method ->
        val google = method == AuthMethod.GOOGLE
        val tag = if (google) MobileSignInTags.GOOGLE else MobileSignInTags.YANDEX
        QuizzProviderButton(
            text = stringResource(if (google) R.string.auth_google else R.string.auth_yandex),
            icon = if (google) UiR.drawable.ic_quizz_google_g else UiR.drawable.ic_quizz_yandex_id,
            onClick = { onProvider(method) },
            modifier = Modifier.fillMaxWidth().testTag(tag),
            enabled = enabled,
        )
    }
}
