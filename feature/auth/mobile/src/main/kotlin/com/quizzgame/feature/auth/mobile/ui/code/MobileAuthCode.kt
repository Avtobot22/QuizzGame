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
import com.quizzgame.feature.auth.mobile.presentation.MobileSignInIntent
import com.quizzgame.feature.auth.mobile.presentation.MobileSignInState
import com.quizzgame.feature.auth.mobile.presentation.MobileSignInStep
import com.quizzgame.feature.auth.mobile.ui.MobileAuthError
import com.quizzgame.feature.auth.mobile.ui.authFailureText
import com.quizzgame.feature.auth.mobile.ui.tags.MobileSignInTags
import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.AuthMethod

@Composable
internal fun MobileAuthCode(
    step: MobileSignInStep.Code,
    state: MobileSignInState,
    elapsed: Long,
    onIntent: (MobileSignInIntent) -> Unit,
) {
    val failure = state.failure ?: AuthFailure.Expired.takeIf { elapsed >= step.form.expiresAtMillis }
    val expired = step.form.isExpired(elapsed, state.failure)
    val canRequest = state.canRequest(elapsed)
    val canVerify = canRequest && !expired && step.form.complete
    val error = failure?.takeIf { it == AuthFailure.InvalidCode }?.let { authFailureText(it) }
    QuizzOtpCells(
        value = step.form.code,
        onValueChange = { onIntent(MobileSignInIntent.Edit(it)) },
        label = stringResource(R.string.auth_code),
        modifier = Modifier.testTag(MobileSignInTags.CODE),
        length = step.form.challenge.codeLength,
        enabled = !expired && !state.busy,
        errorMessage = error,
        autofill = ContentType.SmsOtpCode.takeIf { step.method == AuthMethod.SMS },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { if (canVerify) onIntent(MobileSignInIntent.Verify) }),
    )
    MobileAuthError(failure)
    QuizzButton(
        { onIntent(MobileSignInIntent.Verify) },
        Modifier.fillMaxWidth().testTag(MobileSignInTags.VERIFY),
        enabled = canVerify,
    ) { Text(stringResource(R.string.auth_verify)) }
    val wait = step.form.resendWaitMillis(state.requestBlockedUntil, elapsed)
    MobileAuthCodeActions(step.method, wait, canRequest, onIntent)
}
