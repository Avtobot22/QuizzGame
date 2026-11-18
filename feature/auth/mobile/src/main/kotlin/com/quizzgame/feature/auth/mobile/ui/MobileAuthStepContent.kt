package com.quizzgame.feature.auth.mobile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzButton
import com.quizzgame.core.designsystem.mobile.QuizzButtonStyle
import com.quizzgame.feature.auth.mobile.R
import com.quizzgame.feature.auth.mobile.ui.tags.MobileSignInTags
import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.domain.entity.OtpIntent
import com.quizzgame.shared.auth.domain.entity.OtpState
import com.quizzgame.shared.auth.domain.entity.OtpStep

@Composable
internal fun MobileAuthStepContent(state: OtpState, elapsed: Long, onIntent: (OtpIntent) -> Unit) {
    val canRequest = !state.busy && elapsed >= state.requestBlockedUntil &&
        state.failure != AuthFailure.StorageRecovery
    when (val step = state.step) {
        OtpStep.Loading -> if (!state.busy) {
            QuizzButton({ onIntent(OtpIntent.Load) }) { Text(stringResource(R.string.auth_retry)) }
        }
        is OtpStep.Methods -> AuthMethods(step, !state.busy, onIntent)
        is OtpStep.Identifier -> AuthIdentifier(step, state, canRequest, onIntent)
        is OtpStep.Code -> AuthCode(step, state, canRequest, elapsed, onIntent)
        OtpStep.Finished -> Unit
    }
}

@Composable
private fun AuthMethods(step: OtpStep.Methods, enabled: Boolean, onIntent: (OtpIntent) -> Unit) {
    val methods = step.choices

    if (methods.isEmpty()) Text(stringResource(R.string.auth_unavailable))
    methods.forEach { method ->
        val tag = if (method == AuthMethod.EMAIL) MobileSignInTags.EMAIL else "auth:${method.name.lowercase()}"
        if (method == AuthMethod.GOOGLE) {
            GoogleAuthButton(Modifier.testTag(tag), enabled) { onIntent(OtpIntent.Choose(method)) }
        } else {
            MethodButton(method, tag, enabled, onIntent)
        }
    }
}

@Composable
private fun MethodButton(
    method: AuthMethod,
    tag: String,
    enabled: Boolean,
    onIntent: (OtpIntent) -> Unit,
) {
    QuizzButton(
        { onIntent(OtpIntent.Choose(method)) },
        Modifier.fillMaxWidth().testTag(tag),
        style = QuizzButtonStyle.OUTLINED,
        enabled = enabled,
    ) {
        Text(
            stringResource(
                when (method) {
                    AuthMethod.EMAIL -> R.string.auth_email
                    AuthMethod.SMS -> R.string.auth_sms
                    AuthMethod.GOOGLE -> R.string.auth_google
                    AuthMethod.YANDEX -> R.string.auth_yandex
                },
            ),
        )
    }
}

@Composable
private fun AuthIdentifier(
    step: OtpStep.Identifier,
    state: OtpState,
    canRequest: Boolean,
    onIntent: (OtpIntent) -> Unit,
) {
    val email = step.method == AuthMethod.EMAIL

    OutlinedTextField(
        value = step.input,
        onValueChange = { onIntent(OtpIntent.Edit(it)) },
        modifier = Modifier.fillMaxWidth().testTag(MobileSignInTags.IDENTIFIER).semantics {
            contentType = if (email) ContentType.EmailAddress else ContentType.PhoneNumber
        },
        label = { Text(stringResource(if (email) R.string.auth_email_label else R.string.auth_phone_label)) },
        keyboardOptions = KeyboardOptions(
            keyboardType = if (email) KeyboardType.Email else KeyboardType.Phone,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onDone = { if (canRequest) onIntent(OtpIntent.Send) }),
        singleLine = true,
        enabled = !state.busy,
        isError = state.failure == AuthFailure.InvalidIdentifier,
    )
    QuizzButton(
        { onIntent(OtpIntent.Send) },
        Modifier.fillMaxWidth().testTag(MobileSignInTags.SEND),
        enabled = canRequest,
    ) {
        Text(stringResource(R.string.auth_send))
    }
}

@Composable
private fun AuthCode(
    step: OtpStep.Code,
    state: OtpState,
    canRequest: Boolean,
    elapsed: Long,
    onIntent: (OtpIntent) -> Unit,
) {
    val expired = elapsed >= step.expiresAtMillis || state.failure == AuthFailure.Expired ||
        state.failure == AuthFailure.Consumed
    Text(stringResource(R.string.auth_sent, step.challenge.maskedDestination))
    if (expired && state.failure != AuthFailure.Expired && state.failure != AuthFailure.Consumed) {
        Text(stringResource(R.string.auth_expired))
    }
    val canVerify = canRequest && !expired && step.code.length == step.challenge.codeLength
    CodeInput(step, !expired && !state.busy, canVerify, onIntent)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        QuizzButton(
            { onIntent(OtpIntent.Verify) },
            Modifier.fillMaxWidth().testTag(MobileSignInTags.VERIFY),
            enabled = canVerify,
        ) { Text(stringResource(R.string.auth_verify)) }
        val wait = (maxOf(step.resendAtMillis, state.requestBlockedUntil) - elapsed).coerceAtLeast(0)
        QuizzButton(
            { onIntent(OtpIntent.Resend) },
            style = QuizzButtonStyle.TEXT,
            enabled = canRequest && wait == 0L,
        ) {
            Text(
                if (wait == 0L) {
                    stringResource(R.string.auth_resend)
                } else {
                    stringResource(
                        R.string.auth_cooldown,
                        (wait + 999) / 1000,
                    )
                },
            )
        }
    }
}

@Composable
private fun CodeInput(
    step: OtpStep.Code,
    enabled: Boolean,
    canVerify: Boolean,
    onIntent: (OtpIntent) -> Unit,
) {
    OutlinedTextField(
        value = step.code,
        onValueChange = { onIntent(OtpIntent.Edit(it)) },
        modifier = Modifier.fillMaxWidth().testTag(MobileSignInTags.CODE).semantics {
            if (step.method == AuthMethod.SMS) contentType = ContentType.SmsOtpCode
        },
        label = { Text(stringResource(R.string.auth_code)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { if (canVerify) onIntent(OtpIntent.Verify) }),
        singleLine = true,
        enabled = enabled,
    )
}
