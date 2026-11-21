package com.quizzgame.feature.authprovider.tv.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvNoticeBanner
import com.quizzgame.core.error.operation.ErrorOperationContext
import com.quizzgame.core.error.operation.OperationKind
import com.quizzgame.core.error.operation.RetrySafety
import com.quizzgame.core.error.ui.common.presentation.errorUiModel
import com.quizzgame.core.error.ui.tv.policy.TvErrorDecisionPolicy
import com.quizzgame.core.ui.status.QuizzNoticeTone
import com.quizzgame.core.ui.status.QuizzStatusIcon
import com.quizzgame.feature.authprovider.tv.R
import com.quizzgame.shared.auth.domain.entity.AuthFailure

@Composable
internal fun TvDeviceAuthError(failure: AuthFailure) {
    if (failure is AuthFailure.General) {
        val spec = TvErrorDecisionPolicy().decide(
            failure.failure,
            ErrorOperationContext(OperationKind.AUTH_FLOW, false, RetrySafety.FORBIDDEN),
        )
        val model = errorUiModel(spec)
        QuizzTvNoticeBanner(tone = QuizzNoticeTone.ERROR, icon = model.icon) {
            Text(model.title, style = MaterialTheme.typography.bodyMedium)
        }
    } else {
        val message = when (failure) {
            AuthFailure.Expired -> R.string.auth_device_expired
            AuthFailure.StorageRecovery -> R.string.auth_device_storage
            AuthFailure.SessionChanged -> R.string.auth_device_session_changed
            is AuthFailure.RateLimited -> R.string.auth_device_rate_limited
            else -> R.string.auth_device_provider_error
        }
        QuizzTvNoticeBanner(tone = QuizzNoticeTone.ERROR, icon = QuizzStatusIcon.AUTH) {
            Text(stringResource(message), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
