package com.quizzgame.core.designsystem.tv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.quizzgame.core.ui.R

@Composable
fun QuizzTvPairingPanel(
    state: PairingState,
    title: String,
    code: String,
    supportingText: String,
    modifier: Modifier = Modifier,
    qrContent: @Composable () -> Unit = {},
    actions: @Composable () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val status = stringResource(
        when (state) {
            PairingState.WAITING -> R.string.quizz_status_waiting
            PairingState.APPROVED -> R.string.quizz_status_approved
            PairingState.EXPIRED -> R.string.quizz_status_expired
            PairingState.DENIED -> R.string.quizz_status_denied
        }
    )
    Surface(
        modifier = modifier.semantics {
            liveRegion = LiveRegionMode.Polite
            stateDescription = status
        },
        shape = MaterialTheme.shapes.extraLarge,
        colors = SurfaceDefaults.colors(containerColor = colors.surfaceVariant, contentColor = colors.onSurface),
    ) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = TvComponentType.panelTitle)
            qrContent()
            Text(code, color = colors.primary, style = TvComponentType.panelCode)
            Text(supportingText, color = colors.onSurfaceVariant, style = TvComponentType.panelHelper)
            actions()
        }
    }
}
