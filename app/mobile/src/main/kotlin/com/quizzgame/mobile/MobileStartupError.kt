package com.quizzgame.mobile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quizzgame.app.startup.AppStartupState
import com.quizzgame.app.ui.tags.AppRootTags
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.ui.mobile.components.QuizzMobileErrorFullScreen

@Composable
internal fun MobileStartupError(state: AppStartupState.Failed, onAction: (ErrorActionKind) -> Unit) {
    Column(Modifier.fillMaxSize().testTag(AppRootTags.STARTUP_FAILED)) {
        if (state.configurationRequired) {
            Text(
                stringResource(R.string.app_network_configuration_required),
                Modifier.padding(24.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        QuizzMobileErrorFullScreen(state.error, onAction, Modifier.weight(1f))
    }
}
