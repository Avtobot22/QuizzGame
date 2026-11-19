package com.quizzgame.feature.play.mobile.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzButton
import com.quizzgame.core.designsystem.mobile.QuizzButtonStyle
import com.quizzgame.core.ui.QuizzIconLabel
import com.quizzgame.feature.play.mobile.R
import com.quizzgame.feature.play.mobile.presentation.MobilePlayIntent
import com.quizzgame.feature.play.mobile.ui.tags.MobilePlayTags
import com.quizzgame.core.ui.R as UiR

@Composable
internal fun MobilePlayConnection(onIntent: (MobilePlayIntent) -> Unit, modifier: Modifier = Modifier) {
    var code by remember { mutableStateOf("") }
    val colors = MaterialTheme.colorScheme
    Card(
        modifier,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow),
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                code,
                { code = it },
                Modifier.fillMaxWidth().testTag(MobilePlayTags.CODE),
                label = { Text(stringResource(R.string.play_code)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                textStyle = MaterialTheme.typography.titleLarge,
                shape = MaterialTheme.shapes.medium,
            )
            QuizzButton(
                { onIntent(MobilePlayIntent.CONNECT) },
                Modifier.fillMaxWidth().testTag(MobilePlayTags.CONNECT),
            ) { Text(stringResource(R.string.play_connect)) }
            MobilePlayQrOption { onIntent(MobilePlayIntent.SCAN) }
            Text(
                stringResource(R.string.play_pending),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MobilePlayQrOption(onScan: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HorizontalDivider(Modifier.weight(1f), color = colors.outlineVariant)
        Text(
            stringResource(R.string.play_or),
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
        )
        HorizontalDivider(Modifier.weight(1f), color = colors.outlineVariant)
    }
    QuizzButton(
        onScan,
        Modifier.fillMaxWidth().testTag(MobilePlayTags.SCAN),
        style = QuizzButtonStyle.OUTLINED,
    ) {
        QuizzIconLabel(UiR.drawable.ic_quizz_scan, tint = LocalContentColor.current) {
            Text(stringResource(R.string.play_scan))
        }
    }
}
