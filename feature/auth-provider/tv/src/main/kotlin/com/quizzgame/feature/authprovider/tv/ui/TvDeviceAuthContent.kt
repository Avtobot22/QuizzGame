package com.quizzgame.feature.authprovider.tv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
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
import com.quizzgame.core.designsystem.tv.QuizzTvProgressStrip
import com.quizzgame.core.navigation.focus.TvEntryFocus
import com.quizzgame.feature.authprovider.tv.R
import com.quizzgame.feature.authprovider.tv.presentation.TvDeviceAuthState
import com.quizzgame.feature.authprovider.tv.ui.tags.TvDeviceAuthTags
import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.device.DeviceAuthorization

@Composable
internal fun TvDeviceAuthContent(state: TvDeviceAuthState, onRetry: () -> Unit, onBack: () -> Unit) {
    when (state) {
        TvDeviceAuthState.Idle -> Unit
        TvDeviceAuthState.Starting -> TvDeviceAuthLoading()
        is TvDeviceAuthState.Waiting -> DeviceInstructions(state.authorization, confirming = false)
        is TvDeviceAuthState.Confirming -> DeviceInstructions(state.authorization, confirming = true)
        is TvDeviceAuthState.Failed -> DeviceFailure(state.failure, onRetry, onBack)
    }
}

@Composable
private fun DeviceInstructions(authorization: DeviceAuthorization, confirming: Boolean) {
    val colors = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            stringResource(R.string.auth_device_scan),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        TvAuthQr(authorization.qrUri, Modifier.size(TvAuthQrSize))
        Text(
            stringResource(R.string.auth_device_open, authorization.verificationUri),
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            authorization.code,
            modifier = Modifier.testTag(TvDeviceAuthTags.CODE),
            color = colors.primary,
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            stringResource(if (confirming) R.string.auth_device_confirming else R.string.auth_device_waiting),
            Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
        )
        if (confirming) QuizzTvProgressStrip(stringResource(R.string.auth_device_confirming))
    }
}

@Composable
private fun DeviceFailure(failure: AuthFailure, onRetry: () -> Unit, onBack: () -> Unit) {
    val focus = remember { FocusRequester() }
    val canRetry = failure != AuthFailure.StorageRecovery
    TvDeviceAuthError(failure)
    QuizzTvActionRow {
        if (canRetry) {
            QuizzTvButton(onRetry, Modifier.focusRequester(focus)) {
                Text(stringResource(R.string.auth_device_retry))
            }
        }
        val back = if (canRetry) Modifier else Modifier.focusRequester(focus)
        QuizzTvButton(onBack, back, style = QuizzTvButtonStyle.SECONDARY) {
            Text(stringResource(R.string.auth_device_back))
        }
    }
    TvEntryFocus(focus)
}
