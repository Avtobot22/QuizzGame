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
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.designsystem.tv.QuizzTvButtonStyle
import com.quizzgame.core.navigation.focus.TvEntryFocus
import com.quizzgame.core.ui.QuizzGroupDivider
import com.quizzgame.core.ui.QuizzIconLabel
import com.quizzgame.feature.auth.tv.R
import com.quizzgame.feature.auth.tv.ui.form.TvAuthIdentifier
import com.quizzgame.feature.auth.tv.ui.loading.TvAuthMethodsLoading
import com.quizzgame.feature.auth.tv.ui.tags.TvSignInTags
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.presentation.OtpIntent
import com.quizzgame.shared.auth.presentation.OtpState
import com.quizzgame.shared.auth.presentation.OtpStep
import com.quizzgame.core.ui.R as UiR

@Composable
internal fun TvAuthStepContent(state: OtpState, elapsed: Long, onBack: () -> Unit, onIntent: (OtpIntent) -> Unit) {
    when (val step = state.step) {
        OtpStep.Loading -> if (state.failure == null) {
            TvAuthMethodsLoading()
        } else {
            TvAuthMethodsStatus(state.failure, onBack) { onIntent(OtpIntent.Load) }
        }
        is OtpStep.Methods -> if (step.choices.isEmpty()) {
            TvAuthMethodsStatus(null, onBack) { onIntent(OtpIntent.Load) }
        } else {
            TvAuthError(state.failure)
            AuthMethods(step, onIntent)
        }
        is OtpStep.Identifier -> TvAuthIdentifier(step, state, elapsed, onIntent, onBack)
        else -> Unit
    }
}

@Composable
private fun AuthMethods(step: OtpStep.Methods, onIntent: (OtpIntent) -> Unit) {
    val (direct, providers) = step.choices.partition(AuthMethod::usesOtp)
    val focus = remember { FocusRequester() }
    val initial = direct.firstOrNull() ?: providers.firstOrNull()
    direct.forEachIndexed { index, method ->
        val modifier = if (method == initial) Modifier.focusRequester(focus) else Modifier
        val style = if (index == 0) QuizzTvButtonStyle.PRIMARY else QuizzTvButtonStyle.SECONDARY
        AuthMethodButton(method, style, modifier, onIntent)
    }
    if (direct.isNotEmpty() && providers.isNotEmpty()) {
        QuizzGroupDivider(MaterialTheme.colorScheme.border.copy(alpha = 0.55f))
    }
    providers.forEach { method ->
        val modifier = if (method == initial) Modifier.focusRequester(focus) else Modifier
        AuthMethodButton(method, QuizzTvButtonStyle.BRANDED, modifier, onIntent)
    }
    TvEntryFocus(focus, initial != null)
}

@Composable
private fun AuthMethodButton(
    method: AuthMethod,
    style: QuizzTvButtonStyle,
    modifier: Modifier,
    onIntent: (OtpIntent) -> Unit,
) {
    val action = when (method) {
        AuthMethod.EMAIL  -> MethodAction(TvSignInTags.EMAIL, R.string.auth_email, UiR.drawable.ic_quizz_mail)
        AuthMethod.SMS    -> MethodAction(TvSignInTags.SMS, R.string.auth_sms, UiR.drawable.ic_quizz_sms)
        AuthMethod.GOOGLE -> MethodAction(TvSignInTags.GOOGLE, R.string.auth_google, UiR.drawable.ic_quizz_google_g)
        AuthMethod.YANDEX -> MethodAction(TvSignInTags.YANDEX, R.string.auth_yandex, UiR.drawable.ic_quizz_yandex_id)
    }
    QuizzTvButton(
        { onIntent(OtpIntent.Choose(method)) },
        modifier.fillMaxWidth().testTag(action.tag),
        style = style,
    ) {
        val tint = if (style == QuizzTvButtonStyle.BRANDED) Color.Unspecified else LocalContentColor.current
        QuizzIconLabel(action.icon, Modifier.fillMaxWidth(), tint = tint) {
            Text(stringResource(action.label), textAlign = TextAlign.Center)
        }
    }
}

private data class MethodAction(val tag: String, @param:StringRes val label: Int, @param:DrawableRes val icon: Int)
