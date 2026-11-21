package com.quizzgame.feature.auth.mobile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzButton
import com.quizzgame.core.designsystem.mobile.QuizzButtonStyle
import com.quizzgame.core.designsystem.mobile.QuizzStatusContent
import com.quizzgame.core.error.operation.ErrorOperationContext
import com.quizzgame.core.error.operation.OperationKind
import com.quizzgame.core.error.operation.RetrySafety
import com.quizzgame.core.error.policy.DefaultErrorDecisionPolicy
import com.quizzgame.core.error.ui.common.presentation.errorUiModel
import com.quizzgame.core.ui.QuizzIconLabel
import com.quizzgame.core.ui.status.QuizzNoticeTone
import com.quizzgame.core.ui.status.QuizzStatusIcon
import com.quizzgame.core.ui.tokens.QuizzIdentityDimensions
import com.quizzgame.feature.auth.mobile.R
import com.quizzgame.feature.auth.mobile.ui.tags.MobileSignInTags
import com.quizzgame.shared.auth.domain.entity.AuthFailure

@Composable
internal fun MobileAuthMethodsStatus(
    failure: AuthFailure?,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val icon = when (failure) {
        is AuthFailure.General -> generalErrorModel(failure).icon
        null -> QuizzStatusIcon.CATALOG
        else -> QuizzStatusIcon.AUTH
    }
    val tone = if (failure == null) QuizzNoticeTone.NEUTRAL else QuizzNoticeTone.ERROR
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        QuizzStatusContent(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            tone = tone,
            icon = icon,
            actions = { StatusActions(onRetry, onBack) },
        ) {
            val title = if (failure == null) R.string.auth_unavailable else R.string.auth_load_failed
            Text(
                stringResource(title),
                Modifier.semantics { heading() },
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Text(
                if (failure != null) authFailureText(failure) else stringResource(R.string.auth_methods_empty_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun StatusActions(onRetry: () -> Unit, onBack: () -> Unit) {
    Column(
        Modifier.widthIn(max = 280.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        QuizzButton(onRetry, Modifier.fillMaxWidth()) { Text(stringResource(R.string.auth_retry)) }
        QuizzButton(onBack, Modifier.fillMaxWidth(), style = QuizzButtonStyle.TEXT) {
            Text(stringResource(R.string.auth_back))
        }
    }
}

@Composable
internal fun MobileAuthError(failure: AuthFailure?) {
    if (failure == null) return
    val colors = MaterialTheme.colorScheme
    val icon = if (failure is AuthFailure.General) generalErrorModel(failure).icon else QuizzStatusIcon.AUTH
    QuizzIconLabel(
        icon.resource,
        Modifier.fillMaxWidth()
            .background(colors.errorContainer.copy(alpha = 0.45f), MaterialTheme.shapes.medium)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag(MobileSignInTags.ERROR)
            .semantics { liveRegion = LiveRegionMode.Polite },
        iconSize = QuizzIdentityDimensions.actionIcon.dp,
        tint = colors.onErrorContainer,
        alignment = Alignment.Start,
    ) {
        Text(authFailureText(failure), style = MaterialTheme.typography.bodyMedium, color = colors.onErrorContainer)
    }
}

@Composable
internal fun authFailureText(failure: AuthFailure): String = when (failure) {
    is AuthFailure.General -> generalErrorModel(failure).title
    AuthFailure.InvalidIdentifier -> stringResource(R.string.auth_invalid_identifier)
    AuthFailure.InvalidCode       -> stringResource(R.string.auth_invalid_code)
    AuthFailure.Expired           -> stringResource(R.string.auth_expired)
    AuthFailure.Consumed          -> stringResource(R.string.auth_consumed)
    is AuthFailure.RateLimited    -> stringResource(R.string.auth_rate_limited)
    AuthFailure.SessionChanged    -> stringResource(R.string.auth_session_changed)
    AuthFailure.StorageRecovery   -> stringResource(R.string.auth_storage)
    else                          -> stringResource(R.string.auth_provider_error)
}

@Composable
private fun generalErrorModel(failure: AuthFailure.General) = errorUiModel(
    DefaultErrorDecisionPolicy().decide(
        failure.failure,
        ErrorOperationContext(OperationKind.AUTH_FLOW, false, RetrySafety.FORBIDDEN),
    ),
)
