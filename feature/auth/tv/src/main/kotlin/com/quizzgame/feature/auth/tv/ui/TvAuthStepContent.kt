package com.quizzgame.feature.auth.tv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.designsystem.tv.QuizzTvButtonStyle
import com.quizzgame.core.designsystem.tv.QuizzTvCodeInput
import com.quizzgame.core.navigation.focus.TvEntryFocus
import com.quizzgame.feature.auth.tv.R
import com.quizzgame.feature.auth.tv.ui.tags.TvSignInTags
import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.domain.entity.OtpIntent
import com.quizzgame.shared.auth.domain.entity.OtpState
import com.quizzgame.shared.auth.domain.entity.OtpStep

@Composable
internal fun TvAuthStepContent(state: OtpState, elapsed: Long, onIntent: (OtpIntent) -> Unit) {
    val canRequest = !state.busy && elapsed >= state.requestBlockedUntil &&
        state.failure != AuthFailure.StorageRecovery
    when (val step = state.step) {
        OtpStep.Loading -> if (!state.busy) {
            QuizzTvButton({ onIntent(OtpIntent.Load) }) { Text(stringResource(R.string.auth_retry)) }
        }
        is OtpStep.Methods -> AuthMethods(step, onIntent)
        is OtpStep.Identifier -> AuthIdentifier(step, state, canRequest, onIntent)
        is OtpStep.Code -> AuthCode(step, state, canRequest, elapsed, onIntent)
        OtpStep.Finished -> Unit
    }
}

@Composable
private fun AuthMethods(step: OtpStep.Methods, onIntent: (OtpIntent) -> Unit) {
    val methods = step.choices.filter { it == AuthMethod.EMAIL || it == AuthMethod.SMS }
    val focus = remember { FocusRequester() }
    Text(stringResource(if (methods.isEmpty()) R.string.auth_unavailable else R.string.auth_choose))
    methods.forEachIndexed { index, method ->
        val tag = if (method == AuthMethod.EMAIL) TvSignInTags.EMAIL else TvSignInTags.SMS
        QuizzTvButton(
            { onIntent(OtpIntent.Choose(method)) },
            (if (index == 0) Modifier.focusRequester(focus) else Modifier).testTag(tag),
        ) {
            Text(stringResource(if (method == AuthMethod.EMAIL) R.string.auth_email else R.string.auth_sms))
        }
    }
    TvEntryFocus(focus, methods.isNotEmpty())
}

@Composable
private fun AuthIdentifier(
    step: OtpStep.Identifier,
    state: OtpState,
    canRequest: Boolean,
    onIntent: (OtpIntent) -> Unit,
) {
    val email = step.method == AuthMethod.EMAIL
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = remember { FocusRequester() }
    OutlinedTextField(
        value = step.input,
        onValueChange = { onIntent(OtpIntent.Edit(it)) },
        modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth()
            .focusRequester(focus).testTag(TvSignInTags.IDENTIFIER).onPreviewKeyEvent { key ->
                if (key.key == Key.DirectionCenter && key.type == KeyEventType.KeyDown && !state.busy) {
                    keyboard?.show()
                    true
                } else {
                    false
                }
            },
        label = { Text(stringResource(if (email) R.string.auth_email_label else R.string.auth_phone_label)) },
        keyboardOptions = KeyboardOptions(keyboardType = if (email) KeyboardType.Email else KeyboardType.Phone),
        singleLine = true,
        enabled = !state.busy,
        isError = state.failure == AuthFailure.InvalidIdentifier,
    )
    QuizzTvButton({ onIntent(OtpIntent.Send) }, Modifier.testTag(TvSignInTags.SEND), enabled = canRequest) {
        Text(stringResource(R.string.auth_send))
    }
    TvEntryFocus(focus, true)
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
    val verifyFocus = remember { FocusRequester() }
    CodeInput(step, !expired && !state.busy, canVerify, onIntent)
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        QuizzTvButton(
            { onIntent(OtpIntent.Verify) },
            Modifier.focusRequester(verifyFocus).testTag(TvSignInTags.VERIFY),
            enabled = canVerify,
        ) { Text(stringResource(R.string.auth_verify)) }
        val wait = (maxOf(step.resendAtMillis, state.requestBlockedUntil) - elapsed).coerceAtLeast(0)
        QuizzTvButton(
            { onIntent(OtpIntent.Resend) },
            style = QuizzTvButtonStyle.SECONDARY,
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
    TvEntryFocus(verifyFocus, canVerify)
}

@Composable
private fun CodeInput(
    step: OtpStep.Code,
    enabled: Boolean,
    canVerify: Boolean,
    onIntent: (OtpIntent) -> Unit,
) {
    QuizzTvCodeInput(
        value = step.code,
        onValueChange = { onIntent(OtpIntent.Edit(it)) },
        onComplete = {},
        length = step.challenge.codeLength,
        characters = "0123456789",
        label = stringResource(R.string.auth_code),
        modifier = Modifier.testTag(TvSignInTags.CODE),
        enabled = enabled,
        requestInitialFocus = enabled && !canVerify,
    )
}
