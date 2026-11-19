package com.quizzgame.feature.auth.tv.ui.intro

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.ui.QuizzSceneIllustration
import com.quizzgame.feature.auth.tv.R
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.presentation.OtpState
import com.quizzgame.shared.auth.presentation.OtpStep
import com.quizzgame.core.ui.R as UiR

@Composable
internal fun TvAuthIntro(state: OtpState, recovery: Boolean) {
    val step = state.step
    val title = when {
        step is OtpStep.Identifier -> if (step.method == AuthMethod.EMAIL) R.string.auth_email else R.string.auth_sms
        else -> R.string.auth_title
    }
    val subtitle = when {
        step is OtpStep.Identifier -> stringResource(R.string.auth_code_explanation)
        recovery -> stringResource(R.string.auth_reauth)
        step is OtpStep.Methods && step.choices.isNotEmpty() -> stringResource(R.string.auth_choose)
        else -> null
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (step !is OtpStep.Identifier) TvAuthArtwork()
        TvAuthHeading(stringResource(title), subtitle)
    }
}

@Composable
internal fun TvAuthHeading(title: String, subtitle: String?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            title,
            Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
        )
        if (subtitle != null) {
            Text(
                subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun TvAuthArtwork() {
    val colors = MaterialTheme.colorScheme
    QuizzSceneIllustration(colors.primary, colors.tertiary, Modifier.size(100.dp)) {
        Image(
            painterResource(UiR.drawable.ic_quizz_monochrome),
            contentDescription = null,
            modifier = Modifier.size(47.dp),
            colorFilter = ColorFilter.tint(colors.primary),
        )
    }
}

@Composable
internal fun TvAuthSuccess() {
    val colors = MaterialTheme.colorScheme
    QuizzSceneIllustration(colors.primary, colors.tertiary, Modifier.size(140.dp)) {
        Icon(painterResource(UiR.drawable.ic_quizz_check), null, Modifier.size(54.dp), colors.primary)
    }
    TvAuthHeading(stringResource(R.string.auth_signed_in), stringResource(R.string.auth_opening))
}
