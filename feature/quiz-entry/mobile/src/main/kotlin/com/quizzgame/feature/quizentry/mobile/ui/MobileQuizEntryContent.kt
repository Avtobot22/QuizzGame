package com.quizzgame.feature.quizentry.mobile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzMobileFavoriteMark
import com.quizzgame.core.designsystem.mobile.QuizzMobileLayout
import com.quizzgame.core.designsystem.mobile.media.QuizzMobileBadge
import com.quizzgame.core.designsystem.mobile.media.QuizzMobileMedia
import com.quizzgame.core.designsystem.mobile.media.readableColor
import com.quizzgame.feature.quizentry.mobile.R
import com.quizzgame.feature.quizentry.mobile.ui.tags.MobileQuizEntryTags
import com.quizzgame.shared.catalog.domain.entity.QuizEntry

@Composable
internal fun MobileQuizEntryContent(entry: QuizEntry, onConnect: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = QuizzMobileLayout.contentWidth).fillMaxSize()
                .verticalScroll(rememberScrollState()).padding(QuizzMobileLayout.pageGutter),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            QuizzMobileMedia(
                entry.cover,
                Modifier.fillMaxWidth().aspectRatio(QuizzMobileLayout.COVER_ASPECT_RATIO)
                    .clip(MaterialTheme.shapes.extraLarge),
                fadeTo = MaterialTheme.colorScheme.background,
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MobileQuizEntryLabels(entry)
                Text(
                    entry.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = entry.accentColor.readableColor(
                        MaterialTheme.colorScheme.onSurface,
                        MaterialTheme.colorScheme.background,
                    ),
                )
                MobileQuizEntryMetadata(entry)
            }
            if (entry.subtitle != null || entry.description != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    entry.subtitle?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
                    entry.description?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            MobileQuizEntryStatus(entry.user)
            MobileQuizEntryConnection(entry.available, onConnect)
        }
    }
}

@Composable
private fun MobileQuizEntryLabels(entry: QuizEntry) {
    if (entry.badge == null && !entry.user.bookmarked) return
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        entry.badge?.let {
            QuizzMobileBadge(
                it.text,
                Modifier.align(Alignment.CenterVertically),
                foreground = it.foreground,
                background = it.background,
                live = it.live,
            )
        }
        if (entry.user.bookmarked) {
            Row(
                Modifier.align(Alignment.CenterVertically).testTag(MobileQuizEntryTags.FAVORITE),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                QuizzMobileFavoriteMark(contentDescription = null)
                Text(
                    stringResource(R.string.entry_bookmarked),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
