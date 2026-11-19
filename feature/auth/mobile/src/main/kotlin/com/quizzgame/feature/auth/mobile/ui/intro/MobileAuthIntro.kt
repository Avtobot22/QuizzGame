package com.quizzgame.feature.auth.mobile.ui.intro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quizzgame.core.ui.QuizzSceneIllustration
import com.quizzgame.feature.auth.mobile.R
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.presentation.OtpStep
import com.quizzgame.core.ui.R as UiR

@Composable
internal fun MobileAuthIntro(step: OtpStep, recovery: Boolean, compact: Boolean) {
    val colors = MaterialTheme.colorScheme
    val title = when (step) {
        is OtpStep.Identifier -> if (step.method == AuthMethod.EMAIL) R.string.auth_email else R.string.auth_sms
        is OtpStep.Code       -> R.string.auth_enter_code
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
            if (step is OtpStep.Identifier || step is OtpStep.Code) AuthPhase(step is OtpStep.Code)
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
private fun authSupporting(step: OtpStep, recovery: Boolean): String? {
    return when {
        step is OtpStep.Identifier -> stringResource(R.string.auth_code_explanation)
        step is OtpStep.Code       -> stringResource(R.string.auth_sent, step.challenge.maskedDestination)
        recovery                   -> stringResource(R.string.auth_reauth)
        step is OtpStep.Methods && step.choices.isNotEmpty() -> stringResource(R.string.auth_choose)
        else -> null
    }
}

@Composable
private fun AuthArtwork(step: OtpStep) {
    val colors = MaterialTheme.colorScheme
    val icon = when (step) {
        is OtpStep.Identifier ->
            if (step.method == AuthMethod.EMAIL) UiR.drawable.ic_quizz_mail else UiR.drawable.ic_quizz_sms
        is OtpStep.Code -> UiR.drawable.ic_quizz_status_auth
        else -> UiR.drawable.ic_quizz_monochrome
    }
    val form = step is OtpStep.Identifier || step is OtpStep.Code
    QuizzSceneIllustration(colors.primary, colors.tertiary, Modifier.size(if (form) 60.dp else 88.dp)) {
        Icon(painterResource(icon), null, Modifier.size(if (form) 27.dp else 42.dp), colors.primary)
    }
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
    Column(
        Modifier.verticalScroll(rememberScrollState()).padding(24.dp).semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        QuizzSceneIllustration(colors.primary, colors.tertiary, Modifier.size(148.dp)) {
            Icon(painterResource(UiR.drawable.ic_quizz_check), null, Modifier.size(54.dp), colors.primary)
        }
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
