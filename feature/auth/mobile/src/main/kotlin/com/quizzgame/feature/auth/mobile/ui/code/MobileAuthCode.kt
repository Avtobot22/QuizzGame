package com.quizzgame.feature.auth.mobile.ui.code

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.quizzgame.core.designsystem.mobile.QuizzButton
import com.quizzgame.core.designsystem.mobile.QuizzOtpCells
import com.quizzgame.feature.auth.mobile.R
import com.quizzgame.feature.auth.mobile.ui.MobileAuthError
import com.quizzgame.feature.auth.mobile.ui.authFailureText
import com.quizzgame.feature.auth.mobile.ui.tags.MobileSignInTags
import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.presentation.OtpIntent
import com.quizzgame.shared.auth.presentation.OtpState
import com.quizzgame.shared.auth.presentation.OtpStep

@Composable
internal fun MobileAuthCode(
    step: OtpStep.Code,
    state: OtpState,
    elapsed: Long,
    onIntent: (OtpIntent) -> Unit,
) {
    val failure = state.failure ?: AuthFailure.Expired.takeIf { elapsed >= step.expiresAtMillis }
    val expired = state.isCodeExpired(step, elapsed)
    val canRequest = state.canRequest(elapsed)
    val canVerify = canRequest && !expired && step.code.length == step.challenge.codeLength
    val error = failure?.takeIf { it == AuthFailure.InvalidCode }?.let { authFailureText(it) }
    QuizzOtpCells(
        value = step.code,
        onValueChange = { onIntent(OtpIntent.Edit(it)) },
        label = stringResource(R.string.auth_code),
        modifier = Modifier.testTag(MobileSignInTags.CODE),
        length = step.challenge.codeLength,
        enabled = !expired && !state.busy,
        errorMessage = error,
        autofill = ContentType.SmsOtpCode.takeIf { step.method == AuthMethod.SMS },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { if (canVerify) onIntent(OtpIntent.Verify) }),
    )
    MobileAuthError(failure)
    QuizzButton(
        { onIntent(OtpIntent.Verify) },
        Modifier.fillMaxWidth().testTag(MobileSignInTags.VERIFY),
        enabled = canVerify,
    ) { Text(stringResource(R.string.auth_verify)) }
    val wait = state.resendWaitMillis(step, elapsed)
    MobileAuthCodeActions(step.method, wait, canRequest, onIntent)
}
