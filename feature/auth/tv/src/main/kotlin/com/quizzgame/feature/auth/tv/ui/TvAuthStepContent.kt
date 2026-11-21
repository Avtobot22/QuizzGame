package com.quizzgame.feature.auth.tv.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.designsystem.tv.QuizzTvButtonStyle
import com.quizzgame.core.navigation.focus.TvEntryFocus
import com.quizzgame.core.ui.QuizzGroupDivider
import com.quizzgame.core.ui.QuizzIconLabel
import com.quizzgame.core.ui.tokens.QuizzIdentityDimensions
import com.quizzgame.feature.auth.tv.R
import com.quizzgame.feature.auth.tv.presentation.TvSignInIntent
import com.quizzgame.feature.auth.tv.presentation.TvSignInState
import com.quizzgame.feature.auth.tv.presentation.TvSignInStep
import com.quizzgame.feature.auth.tv.ui.form.TvAuthIdentifier
import com.quizzgame.feature.auth.tv.ui.loading.TvAuthMethodsLoading
import com.quizzgame.feature.auth.tv.ui.tags.TvSignInTags
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.core.ui.R as UiR

@Composable
internal fun TvAuthStepContent(
    state: TvSignInState,
    elapsed: Long,
    onBack: () -> Unit,
    onIntent: (TvSignInIntent) -> Unit,
    onGoogle: () -> Unit,
) {
    when (val step = state.step) {
        TvSignInStep.Loading -> if (state.failure == null) {
            TvAuthMethodsLoading()
        } else {
            TvAuthMethodsStatus(state.failure, onBack) { onIntent(TvSignInIntent.Load) }
        }
        is TvSignInStep.Methods -> if (step.otp.isEmpty() && !step.google) {
            TvAuthMethodsStatus(null, onBack) { onIntent(TvSignInIntent.Load) }
        } else {
            TvAuthError(state.failure)
            AuthMethods(step, { onIntent(TvSignInIntent.ChooseOtp(it)) }, onGoogle)
        }
        is TvSignInStep.Identifier -> TvAuthIdentifier(step, state, elapsed, onIntent, onBack)
        else -> Unit
    }
}

@Composable
private fun AuthMethods(
    step: TvSignInStep.Methods,
    onOtp: (AuthMethod) -> Unit,
    onGoogle: () -> Unit,
) {
    val focus = remember { FocusRequester() }
    val initial = step.otp.firstOrNull() ?: AuthMethod.GOOGLE.takeIf { step.google }
    step.otp.forEachIndexed { index, method ->
        val modifier = if (method == initial) Modifier.focusRequester(focus) else Modifier
        val style = if (index == 0) QuizzTvButtonStyle.PRIMARY else QuizzTvButtonStyle.SECONDARY
        AuthMethodButton(method, style, modifier) { onOtp(method) }
    }
    if (step.otp.isNotEmpty() && step.google) {
        QuizzGroupDivider(MaterialTheme.colorScheme.border.copy(alpha = 0.55f))
    }
    if (step.google) {
        val modifier = if (initial == AuthMethod.GOOGLE) Modifier.focusRequester(focus) else Modifier
        AuthMethodButton(AuthMethod.GOOGLE, QuizzTvButtonStyle.BRANDED, modifier, onGoogle)
    }
    TvEntryFocus(focus, initial != null)
}

@Composable
private fun AuthMethodButton(
    method: AuthMethod,
    style: QuizzTvButtonStyle,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val action = when (method) {
        AuthMethod.EMAIL  -> MethodAction(TvSignInTags.EMAIL, R.string.auth_email, UiR.drawable.ic_quizz_mail)
        AuthMethod.SMS    -> MethodAction(TvSignInTags.SMS, R.string.auth_sms, UiR.drawable.ic_quizz_sms)
        AuthMethod.GOOGLE -> MethodAction(TvSignInTags.GOOGLE, R.string.auth_google, UiR.drawable.ic_quizz_google_g)
        AuthMethod.YANDEX -> return
    }
    QuizzTvButton(
        onClick,
        modifier.fillMaxWidth().testTag(action.tag),
        style = style,
    ) {
        val tint = if (style == QuizzTvButtonStyle.BRANDED) Color.Unspecified else LocalContentColor.current
        QuizzIconLabel(
            action.icon,
            Modifier.fillMaxWidth(),
            iconSize = QuizzIdentityDimensions.actionIcon.dp,
            tint = tint,
        ) {
            Text(stringResource(action.label), textAlign = TextAlign.Center)
        }
    }
}

private data class MethodAction(val tag: String, @param:StringRes val label: Int, @param:DrawableRes val icon: Int)
