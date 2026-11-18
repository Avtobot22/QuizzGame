package com.quizzgame.feature.auth.mobile.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.quizzgame.core.error.operation.ErrorOperationContext
import com.quizzgame.core.error.operation.OperationKind
import com.quizzgame.core.error.operation.RetrySafety
import com.quizzgame.core.error.policy.DefaultErrorDecisionPolicy
import com.quizzgame.core.error.presentation.ErrorSurface
import com.quizzgame.core.error.ui.mobile.components.QuizzMobileErrorInline
import com.quizzgame.feature.auth.mobile.R
import com.quizzgame.shared.auth.domain.entity.AuthFailure

@Composable
internal fun MobileAuthError(failure: AuthFailure?) {
    when (failure) {
        null -> Unit
        is AuthFailure.General -> {
            val spec = DefaultErrorDecisionPolicy().decide(
                failure.failure,
                ErrorOperationContext(OperationKind.AUTH_FLOW, false, RetrySafety.FORBIDDEN),
            )
            QuizzMobileErrorInline(spec.copy(surface = ErrorSurface.INLINE, actions = emptyList()), {})
        }
        else -> Text(
            stringResource(authErrorText(failure)),
            Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            color = MaterialTheme.colorScheme.error,
        )
    }
}

private fun authErrorText(failure: AuthFailure): Int = when (failure) {
    AuthFailure.InvalidIdentifier -> R.string.auth_invalid_identifier
    AuthFailure.InvalidCode -> R.string.auth_invalid_code
    AuthFailure.Expired -> R.string.auth_expired
    AuthFailure.Consumed -> R.string.auth_consumed
    is AuthFailure.RateLimited -> R.string.auth_rate_limited
    AuthFailure.SessionChanged -> R.string.auth_session_changed
    AuthFailure.StorageRecovery -> R.string.auth_storage
    else -> R.string.auth_provider_error
}
