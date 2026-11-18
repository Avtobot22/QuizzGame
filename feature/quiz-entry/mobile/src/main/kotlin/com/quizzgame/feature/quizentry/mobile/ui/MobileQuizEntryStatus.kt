package com.quizzgame.feature.quizentry.mobile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzMobileProgressTrack
import com.quizzgame.feature.quizentry.mobile.R
import com.quizzgame.feature.quizentry.mobile.ui.tags.MobileQuizEntryTags
import com.quizzgame.shared.catalog.domain.entity.PassingStatus
import com.quizzgame.shared.catalog.domain.entity.QuizUserState
import java.text.NumberFormat

@Composable
internal fun MobileQuizEntryStatus(user: QuizUserState) {
    when (user.status) {
        PassingStatus.NOT_STARTED -> Unit
        PassingStatus.IN_PROGRESS -> MobileQuizEntryProgress(user.progress)
        PassingStatus.COMPLETED   -> MobileQuizEntryCompleted()
    }
}

@Composable
private fun MobileQuizEntryProgress(progress: Double) {
    val colors = MaterialTheme.colorScheme
    val locale = LocalConfiguration.current.locales[0]
    val percent = remember(locale, progress) { NumberFormat.getPercentInstance(locale).format(progress) }
    Surface(
        Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = colors.primaryContainer,
        contentColor = colors.onPrimaryContainer,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.entry_in_progress),
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    percent,
                    Modifier.clearAndSetSemantics {},
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            QuizzMobileProgressTrack(progress.toFloat(), Modifier.testTag(MobileQuizEntryTags.PROGRESS))
        }
    }
}

@Composable
private fun MobileQuizEntryCompleted() {
    val colors = MaterialTheme.colorScheme
    Surface(
        Modifier.fillMaxWidth().testTag(MobileQuizEntryTags.COMPLETED),
        shape = MaterialTheme.shapes.extraLarge,
        color = colors.tertiaryContainer,
        contentColor = colors.onTertiaryContainer,
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(Modifier.size(48.dp).background(colors.tertiary, CircleShape), contentAlignment = Alignment.Center) {
                Icon(
                    painterResource(R.drawable.ic_quizz_trophy),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = colors.onTertiary,
                )
            }
            Text(stringResource(R.string.entry_completed), style = MaterialTheme.typography.titleLarge)
        }
    }
}
