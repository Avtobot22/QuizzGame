package com.quizzgame.feature.auth.mobile.ui.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
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
import com.quizzgame.core.designsystem.mobile.QuizzButton
import com.quizzgame.feature.auth.mobile.R
import com.quizzgame.feature.auth.mobile.ui.MobileAuthError
import com.quizzgame.feature.auth.mobile.ui.tags.MobileSignInTags
import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.presentation.OtpIntent
import com.quizzgame.shared.auth.presentation.OtpState
import com.quizzgame.shared.auth.presentation.OtpStep

@Composable
internal fun MobileAuthIdentifier(
    step: OtpStep.Identifier,
    state: OtpState,
    elapsed: Long,
    onIntent: (OtpIntent) -> Unit,
) {
    val email = step.method == AuthMethod.EMAIL
    val canSend = state.canRequest(elapsed) && step.input.isNotBlank()

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
        keyboardActions = KeyboardActions(onDone = { if (canSend) onIntent(OtpIntent.Send) }),
        shape = MaterialTheme.shapes.large,
        singleLine = true,
        enabled = !state.busy,
        isError = state.failure == AuthFailure.InvalidIdentifier,
    )
    MobileAuthError(state.identifierFailure(elapsed))
    QuizzButton(
        { onIntent(OtpIntent.Send) },
        Modifier.fillMaxWidth().testTag(MobileSignInTags.SEND),
        enabled = canSend,
    ) {
        Text(stringResource(R.string.auth_send))
    }
}
