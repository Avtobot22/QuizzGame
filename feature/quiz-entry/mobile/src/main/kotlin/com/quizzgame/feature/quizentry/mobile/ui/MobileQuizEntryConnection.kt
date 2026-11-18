package com.quizzgame.feature.quizentry.mobile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzButton
import com.quizzgame.feature.quizentry.mobile.R
import com.quizzgame.feature.quizentry.mobile.ui.tags.MobileQuizEntryTags
import com.quizzgame.core.ui.R as UiR

@Composable
internal fun MobileQuizEntryConnection(available: Boolean, onConnect: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (!available) {
            Surface(
                Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = colors.surfaceContainerHigh,
            ) {
                Text(
                    stringResource(R.string.entry_unavailable),
                    Modifier.fillMaxWidth().padding(12.dp).testTag(MobileQuizEntryTags.UNAVAILABLE),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
        QuizzButton(onConnect, Modifier.fillMaxWidth().testTag(MobileQuizEntryTags.CONNECT), enabled = available) {
            Icon(painterResource(UiR.drawable.ic_quizz_room), null, Modifier.size(20.dp))
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.entry_connect))
        }
    }
}
