package com.quizzgame.feature.auth.tv.ui.intro

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvStatusContent
import com.quizzgame.core.designsystem.tv.QuizzTvStatusCopy
import com.quizzgame.core.ui.QuizzAuthArtwork
import com.quizzgame.core.ui.status.QuizzStatusIcon
import com.quizzgame.core.ui.tokens.QuizzIdentityDimensions
import com.quizzgame.feature.auth.tv.R
import com.quizzgame.feature.auth.tv.presentation.TvSignInState
import com.quizzgame.feature.auth.tv.presentation.TvSignInStep
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.core.ui.R as UiR

@Composable
internal fun TvAuthIntro(state: TvSignInState, recovery: Boolean) {
    val step = state.step
    val title = when {
        step is TvSignInStep.Identifier ->
            if (step.method == AuthMethod.EMAIL) R.string.auth_email else R.string.auth_sms
        else -> R.string.auth_title
    }
    val subtitle = when {
        step is TvSignInStep.Identifier -> stringResource(R.string.auth_code_explanation)
        recovery -> stringResource(R.string.auth_reauth)
        step is TvSignInStep.Methods &&
            (step.otp.isNotEmpty() || step.google) -> stringResource(R.string.auth_choose)
        else -> null
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val icon = when {
            step is TvSignInStep.Identifier && step.method == AuthMethod.EMAIL -> UiR.drawable.ic_quizz_mail
            step is TvSignInStep.Identifier -> UiR.drawable.ic_quizz_sms
            else -> UiR.drawable.ic_quizz_monochrome
        }
        TvAuthArtwork(icon)
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
internal fun TvAuthArtwork(@DrawableRes icon: Int) {
    val colors = MaterialTheme.colorScheme
    QuizzAuthArtwork(
        icon,
        colors.primary,
        colors.tertiary,
        QuizzIdentityDimensions.tvArtwork.dp,
        QuizzIdentityDimensions.tvArtworkIcon.dp,
    )
}

@Composable
internal fun TvAuthSuccess() {
    QuizzTvStatusContent(icon = QuizzStatusIcon.CHECK) {
        QuizzTvStatusCopy(stringResource(R.string.auth_signed_in), stringResource(R.string.auth_opening))
    }
}
