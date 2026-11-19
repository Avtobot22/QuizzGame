package com.quizzgame.feature.quizentry.mobile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import com.quizzgame.core.ui.QuizzIconLabel
import com.quizzgame.feature.quizentry.mobile.R
import com.quizzgame.shared.catalog.domain.entity.QuizEntry
import com.quizzgame.core.ui.R as UiR

@Composable
internal fun MobileQuizEntryMetadata(entry: QuizEntry) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        entry.questionCount?.let {
            EntryMetadataChip(UiR.drawable.ic_quizz_quiz, pluralStringResource(R.plurals.entry_questions, it, it))
        }
        entry.durationSeconds?.let {
            val minutes = (it + SECONDS_PER_MINUTE - 1) / SECONDS_PER_MINUTE
            EntryMetadataChip(
                UiR.drawable.ic_quizz_clock,
                pluralStringResource(R.plurals.entry_minutes, minutes, minutes),
            )
        }
    }
}

@Composable
private fun EntryMetadataChip(icon: Int, label: String) {
    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainerLow) {
        QuizzIconLabel(
            icon,
            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            iconSize = 18.dp,
            tint = MaterialTheme.colorScheme.primary,
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

private const val SECONDS_PER_MINUTE = 60
