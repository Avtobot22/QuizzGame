package com.quizzgame.feature.auth.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvActionRow
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.designsystem.tv.QuizzTvButtonStyle
import com.quizzgame.core.designsystem.tv.QuizzTvStatusContent
import com.quizzgame.core.designsystem.tv.QuizzTvStatusCopy
import com.quizzgame.core.error.operation.ErrorOperationContext
import com.quizzgame.core.error.operation.OperationKind
import com.quizzgame.core.error.operation.RetrySafety
import com.quizzgame.core.error.ui.common.presentation.errorUiModel
import com.quizzgame.core.error.ui.tv.policy.TvErrorDecisionPolicy
import com.quizzgame.core.navigation.focus.TvEntryFocus
import com.quizzgame.core.ui.QuizzIconLabel
import com.quizzgame.core.ui.status.QuizzNoticeTone
import com.quizzgame.core.ui.status.QuizzStatusIcon
import com.quizzgame.core.ui.tokens.QuizzIdentityDimensions
import com.quizzgame.feature.auth.tv.R
import com.quizzgame.shared.auth.domain.entity.AuthFailure

@Composable
internal fun TvAuthMethodsStatus(failure: AuthFailure?, onBack: () -> Unit, onRetry: () -> Unit) {
    val focus = remember { FocusRequester() }
    val icon = when (failure) {
        is AuthFailure.General -> generalErrorModel(failure).icon
        null -> QuizzStatusIcon.CATALOG
        else -> QuizzStatusIcon.AUTH
    }
    QuizzTvStatusContent(
        tone = if (failure == null) QuizzNoticeTone.NEUTRAL else QuizzNoticeTone.ERROR,
        icon = icon,
        actions = {
            QuizzTvActionRow {
                QuizzTvButton(onRetry, Modifier.focusRequester(focus)) { Text(stringResource(R.string.auth_retry)) }
                QuizzTvButton(onBack, style = QuizzTvButtonStyle.SECONDARY) {
                    Text(stringResource(R.string.auth_cancel))
                }
            }
        },
    ) {
        QuizzTvStatusCopy(
            stringResource(if (failure == null) R.string.auth_unavailable else R.string.auth_load_failed),
            if (failure == null) stringResource(R.string.auth_methods_empty_hint) else failureText(failure),
        )
    }
    TvEntryFocus(focus)
}

@Composable
internal fun TvAuthError(failure: AuthFailure?, message: String? = null) {
    if (failure == null) return
    val colors = MaterialTheme.colorScheme
    val icon = if (failure is AuthFailure.General) generalErrorModel(failure).icon else QuizzStatusIcon.AUTH
    QuizzIconLabel(
        icon.resource,
        Modifier.widthIn(max = 520.dp)
            .background(colors.errorContainer.copy(alpha = 0.45f), MaterialTheme.shapes.medium)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        iconSize = QuizzIdentityDimensions.actionIcon.dp,
        tint = colors.onErrorContainer,
    ) {
        Text(
            message ?: failureText(failure),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onErrorContainer,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun failureText(failure: AuthFailure): String = when (failure) {
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
    TvErrorDecisionPolicy().decide(
        failure.failure,
        ErrorOperationContext(OperationKind.AUTH_FLOW, false, RetrySafety.FORBIDDEN),
    ),
)
