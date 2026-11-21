package com.quizzgame.feature.quizentry.tv.ui.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.media.QuizzTvBadge
import com.quizzgame.core.designsystem.tv.media.QuizzTvBadgeStyle
import com.quizzgame.core.designsystem.tv.media.readableColor
import com.quizzgame.core.ui.QuizzIconLabel
import com.quizzgame.core.ui.tokens.QuizzTvLayout
import com.quizzgame.feature.quizentry.tv.R
import com.quizzgame.shared.catalog.domain.entity.QuizEntry
import com.quizzgame.core.ui.R as UiR

@Composable
internal fun TvQuizEntryDetails(entry: QuizEntry) {
    val colors = MaterialTheme.colorScheme
    Box(Modifier.heightIn(min = QuizzTvLayout.badgeHeight.dp)) {
        entry.badge?.let {
            QuizzTvBadge(
                it.text,
                it.live,
                foreground = it.foreground,
                background = it.background,
                style = QuizzTvBadgeStyle.EMPHASIZED,
            )
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            entry.title,
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.displaySmall,
            color = entry.accentColor.readableColor(colors.onSurface, colors.background),
        )
        entry.subtitle?.let {
            Text(it, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
        }
    }
    entry.description?.takeUnless { it == entry.subtitle }?.let {
        Text(it, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        entry.questionCount?.let {
            EntryMetadata(UiR.drawable.ic_quizz_quiz, pluralStringResource(R.plurals.quiz_entry_question_count, it, it))
        }
        entry.durationSeconds?.let {
            EntryMetadata(UiR.drawable.ic_quizz_clock, stringResource(R.string.quiz_entry_duration, (it + 59) / 60))
        }
        if (entry.user.bookmarked) {
            EntryMetadata(UiR.drawable.ic_quizz_bookmark, stringResource(R.string.quiz_entry_bookmarked))
        }
    }
}

@Composable
private fun EntryMetadata(icon: Int, text: String) {
    QuizzIconLabel(icon, iconSize = 16.dp, gap = 6.dp, tint = MaterialTheme.colorScheme.primary) {
        Text(text, style = MaterialTheme.typography.labelMedium)
    }
}
