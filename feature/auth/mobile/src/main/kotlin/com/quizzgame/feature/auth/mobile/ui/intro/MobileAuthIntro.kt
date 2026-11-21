package com.quizzgame.feature.auth.mobile.ui.intro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzStatusContent
import com.quizzgame.core.ui.QuizzAuthArtwork
import com.quizzgame.core.ui.status.QuizzStatusIcon
import com.quizzgame.core.ui.tokens.QuizzIdentityDimensions
import com.quizzgame.feature.auth.mobile.R
import com.quizzgame.feature.auth.mobile.presentation.MobileSignInStep
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.core.ui.R as UiR

@Composable
internal fun MobileAuthIntro(step: MobileSignInStep, recovery: Boolean, compact: Boolean) {
    val colors = MaterialTheme.colorScheme
    val title = when (step) {
        is MobileSignInStep.Identifier ->
            if (step.method == AuthMethod.EMAIL) R.string.auth_email else R.string.auth_sms
        is MobileSignInStep.Code       -> R.string.auth_enter_code
        else                  -> R.string.auth_title
    }
    val supporting = authSupporting(step, recovery)
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (!compact) {
            AuthArtwork(step)
            if (step is MobileSignInStep.Identifier || step is MobileSignInStep.Code) {
                AuthPhase(step is MobileSignInStep.Code)
            }
        }
        Text(
            stringResource(title),
            Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        if (supporting != null) {
            Text(
                supporting,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun authSupporting(step: MobileSignInStep, recovery: Boolean): String? {
    return when {
        step is MobileSignInStep.Identifier -> stringResource(R.string.auth_code_explanation)
        step is MobileSignInStep.Code       -> stringResource(R.string.auth_sent, step.form.challenge.maskedDestination)
        recovery                   -> stringResource(R.string.auth_reauth)
        step is MobileSignInStep.Methods &&
            (step.otp.isNotEmpty() || step.providers.isNotEmpty()) -> stringResource(R.string.auth_choose)
        else -> null
    }
}

@Composable
private fun AuthArtwork(step: MobileSignInStep) {
    val colors = MaterialTheme.colorScheme
    val icon = when (step) {
        is MobileSignInStep.Identifier ->
            if (step.method == AuthMethod.EMAIL) UiR.drawable.ic_quizz_mail else UiR.drawable.ic_quizz_sms
        is MobileSignInStep.Code -> UiR.drawable.ic_quizz_status_auth
        else -> UiR.drawable.ic_quizz_monochrome
    }
    QuizzAuthArtwork(
        icon,
        colors.primary,
        colors.tertiary,
        QuizzIdentityDimensions.mobileArtwork.dp,
        QuizzIdentityDimensions.mobileArtworkIcon.dp,
    )
}

@Composable
private fun AuthPhase(code: Boolean) {
    val colors = MaterialTheme.colorScheme
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(width = 16.dp, height = 3.dp).background(colors.primary, MaterialTheme.shapes.small))
        val second = if (code) colors.primary else colors.outlineVariant
        Box(Modifier.size(width = 16.dp, height = 3.dp).background(second, MaterialTheme.shapes.small))
        Text(
            stringResource(R.string.auth_phase, if (code) 2 else 1),
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
        )
    }
}

@Composable
internal fun MobileAuthSuccess() {
    val colors = MaterialTheme.colorScheme
    QuizzStatusContent(Modifier.verticalScroll(rememberScrollState()), icon = QuizzStatusIcon.CHECK) {
        Text(
            stringResource(R.string.auth_signed_in),
            Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(R.string.auth_opening),
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}
