package com.quizzgame.feature.auth.tv.ui.code

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvActionRow
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.designsystem.tv.QuizzTvButtonStyle
import com.quizzgame.core.designsystem.tv.QuizzTvCenteredContent
import com.quizzgame.core.designsystem.tv.QuizzTvCodeInput
import com.quizzgame.core.designsystem.tv.QuizzTvProgressStrip
import com.quizzgame.core.navigation.focus.TvEntryFocus
import com.quizzgame.feature.auth.tv.R
import com.quizzgame.feature.auth.tv.presentation.TvSignInIntent
import com.quizzgame.feature.auth.tv.presentation.TvSignInState
import com.quizzgame.feature.auth.tv.presentation.TvSignInStep
import com.quizzgame.feature.auth.tv.ui.TvAuthError
import com.quizzgame.feature.auth.tv.ui.intro.TvAuthArtwork
import com.quizzgame.feature.auth.tv.ui.intro.TvAuthHeading
import com.quizzgame.feature.auth.tv.ui.tags.TvSignInTags
import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.core.ui.R as UiR

@Composable
internal fun TvCodeScreen(
    step: TvSignInStep.Code,
    state: TvSignInState,
    elapsed: Long,
    onBack: () -> Unit,
    onIntent: (TvSignInIntent) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val failure = state.failure ?: AuthFailure.Expired.takeIf { elapsed >= step.form.expiresAtMillis }
    val expired = step.form.isExpired(elapsed, state.failure)
    val canRequest = state.canRequest(elapsed)
    val canVerify = canRequest && !expired && step.form.complete
    QuizzTvCenteredContent {
        TvAuthArtwork(UiR.drawable.ic_quizz_status_auth)
        TvAuthHeading(
            stringResource(R.string.auth_enter_code),
            stringResource(R.string.auth_sent, step.form.challenge.maskedDestination),
        )
        Column(
            Modifier.widthIn(max = 780.dp).fillMaxWidth().testTag(TvSignInTags.CODE_SCREEN)
                .background(colors.surfaceVariant.copy(alpha = 0.35f), MaterialTheme.shapes.extraLarge)
                .padding(horizontal = 28.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            key(step.form.challenge.id) {
                QuizzTvCodeInput(
                    value = step.form.code,
                    onValueChange = { onIntent(TvSignInIntent.Edit(it)) },
                    onComplete = {},
                    length = step.form.challenge.codeLength,
                    characters = "0123456789",
                    label = stringResource(R.string.auth_code),
                    modifier = Modifier.testTag(TvSignInTags.CODE),
                    enabled = !expired && !state.busy,
                    requestInitialFocus = !canVerify,
                )
            }
            TvAuthError(failure)
            if (state.busy) {
                Box(Modifier.widthIn(max = 400.dp).fillMaxWidth().height(4.dp)) {
                    QuizzTvProgressStrip(stringResource(R.string.auth_busy))
                }
            }
            val wait = step.form.resendWaitMillis(state.requestBlockedUntil, elapsed)
            TvCodeActions(step.method, canVerify, canRequest && wait == 0L, wait, expired, onBack, onIntent)
        }
    }
}

@Composable
private fun TvCodeActions(
    method: AuthMethod,
    canVerify: Boolean,
    canResend: Boolean,
    wait: Long,
    expired: Boolean,
    onBack: () -> Unit,
    onIntent: (TvSignInIntent) -> Unit,
) {
    val verify = remember { FocusRequester() }
    val resend = remember { FocusRequester() }
    val back = remember { FocusRequester() }
    QuizzTvActionRow {
        QuizzTvButton(
            { onIntent(TvSignInIntent.Verify) },
            Modifier.focusRequester(verify).testTag(TvSignInTags.VERIFY),
            enabled = canVerify,
        ) { Text(stringResource(R.string.auth_verify)) }
        QuizzTvButton(
            { onIntent(TvSignInIntent.RequestCode) },
            modifier = Modifier.focusRequester(resend).testTag(TvSignInTags.RESEND),
            style = QuizzTvButtonStyle.SECONDARY,
            enabled = canResend,
        ) {
            val label = if (wait > 0) {
                stringResource(R.string.auth_cooldown, (wait + 999) / 1000)
            } else {
                stringResource(R.string.auth_resend)
            }
            Text(label)
        }
        QuizzTvButton(onBack, Modifier.focusRequester(back), style = QuizzTvButtonStyle.QUIET) {
            val label = if (method == AuthMethod.EMAIL) R.string.auth_change_email else R.string.auth_change_phone
            Text(stringResource(label))
        }
    }
    TvEntryFocus(verify, canVerify)
    TvEntryFocus(resend, expired && canResend)
    TvEntryFocus(back, expired && !canResend)
}
