package com.quizzgame.feature.auth.tv.ui.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvActionRow
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.designsystem.tv.QuizzTvButtonStyle
import com.quizzgame.core.navigation.focus.TvEntryFocus
import com.quizzgame.feature.auth.tv.R
import com.quizzgame.feature.auth.tv.presentation.TvSignInIntent
import com.quizzgame.feature.auth.tv.presentation.TvSignInState
import com.quizzgame.feature.auth.tv.presentation.TvSignInStep
import com.quizzgame.feature.auth.tv.ui.TvAuthError
import com.quizzgame.feature.auth.tv.ui.tags.TvSignInTags
import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.AuthMethod

@Composable
internal fun TvAuthIdentifier(
    step: TvSignInStep.Identifier,
    state: TvSignInState,
    elapsed: Long,
    onIntent: (TvSignInIntent) -> Unit,
    onBack: () -> Unit,
) {
    val email = step.method == AuthMethod.EMAIL
    val canSend = state.canRequest(elapsed) && step.input.isNotBlank()
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = remember { FocusRequester() }
    val sendFocus = remember { FocusRequester() }
    val backFocus = remember { FocusRequester() }
    val send = {
        keyboard?.hide()
        onIntent(TvSignInIntent.RequestCode)
    }
    OutlinedTextField(
        value = step.input,
        onValueChange = { onIntent(TvSignInIntent.Edit(it)) },
        modifier = Modifier.fillMaxWidth()
            .focusRequester(focus).testTag(TvSignInTags.IDENTIFIER).onPreviewKeyEvent { key ->
                when (key.key) {
                    Key.DirectionCenter -> {
                        if (key.type == KeyEventType.KeyDown && !state.busy) keyboard?.show()
                        true
                    }
                    Key.DirectionDown -> {
                        if (key.type == KeyEventType.KeyDown) {
                            val target = if (canSend) sendFocus else backFocus
                            target.requestFocus()
                        }
                        true
                    }
                    else -> false
                }
            },
        label = { Text(stringResource(if (email) R.string.auth_email_label else R.string.auth_phone_label)) },
        keyboardOptions = KeyboardOptions(
            keyboardType = if (email) KeyboardType.Email else KeyboardType.Phone,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onDone = { if (canSend) send() }),
        shape = MaterialTheme.shapes.medium,
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = tvIdentifierColors(),
        singleLine = true,
        enabled = !state.busy,
        isError = state.failure == AuthFailure.InvalidIdentifier,
    )
    TvAuthError(state.identifierFailure(elapsed))
    QuizzTvActionRow {
        QuizzTvButton(send, Modifier.focusRequester(sendFocus).testTag(TvSignInTags.SEND), enabled = canSend) {
            Text(stringResource(R.string.auth_send))
        }
        QuizzTvButton(onBack, Modifier.focusRequester(backFocus), style = QuizzTvButtonStyle.SECONDARY) {
            Text(stringResource(R.string.auth_back))
        }
    }
    TvEntryFocus(focus, !state.busy)
}

@Composable
private fun tvIdentifierColors(): TextFieldColors {
    val colors = MaterialTheme.colorScheme
    return OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.onSurface,
        unfocusedBorderColor = colors.borderVariant,
        focusedLabelColor = colors.primary,
        unfocusedLabelColor = colors.onSurfaceVariant,
        focusedTextColor = colors.onSurface,
        unfocusedTextColor = colors.onSurface,
        disabledBorderColor = colors.borderVariant,
        disabledLabelColor = colors.onSurfaceVariant,
        disabledTextColor = colors.onSurface,
        errorBorderColor = colors.error,
        errorLabelColor = colors.error,
        cursorColor = colors.primary,
    )
}
