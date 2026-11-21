package com.quizzgame.feature.play.mobile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quizzgame.core.ui.QuizzSceneIllustration
import com.quizzgame.feature.play.mobile.R
import com.quizzgame.core.ui.R as UiR

@Composable
internal fun MobilePlayIntro(hasRoom: Boolean, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        QuizzSceneIllustration(colors.primary, colors.tertiary, Modifier.size(88.dp)) {
            Icon(painterResource(UiR.drawable.ic_quizz_room), null, Modifier.size(44.dp), tint = colors.primary)
        }
        Text(
            stringResource(if (hasRoom) R.string.play_resume_heading else R.string.play_heading),
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(if (hasRoom) R.string.play_resume_description else R.string.play_description),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
