package com.quizzgame.feature.quizentry.tv.ui.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvActionLabel
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.designsystem.tv.QuizzTvButtonStyle
import com.quizzgame.core.designsystem.tv.media.QuizzTvMedia
import com.quizzgame.core.navigation.focus.TvEntryFocus
import com.quizzgame.core.ui.tokens.QuizzTvLayout
import com.quizzgame.feature.quizentry.tv.R
import com.quizzgame.feature.quizentry.tv.presentation.TvQuizEntryIntent
import com.quizzgame.feature.quizentry.tv.presentation.TvQuizRoomState
import com.quizzgame.feature.quizentry.tv.ui.tags.TvQuizEntryTags
import com.quizzgame.shared.catalog.domain.entity.QuizEntry
import com.quizzgame.core.error.ui.common.R as ErrorR
import com.quizzgame.core.ui.R as UiR

@Composable
internal fun TvQuizEntryContent(
    entry: QuizEntry,
    onBack: () -> Unit,
    modifier: Modifier,
    room: TvQuizRoomState,
    onIntent: (TvQuizEntryIntent) -> Unit,
    onSignIn: (() -> Unit)?,
) {
    val focus = remember { FocusRequester() }
    val colors = MaterialTheme.colorScheme
    Box(modifier.fillMaxSize().testTag(TvQuizEntryTags.CONTENT)) {
        QuizzTvMedia(
            entry.cover,
            Modifier.align(Alignment.CenterEnd).offset(x = QuizzTvLayout.screenInset.dp)
                .fillMaxWidth(0.6f).fillMaxSize(),
            placeholderBrush = Brush.linearGradient(listOf(colors.primaryContainer, colors.surface)),
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(
                    0f to colors.background,
                    0.46f to colors.background,
                    0.72f to colors.background.copy(alpha = 0.5f),
                    1f to colors.background.copy(alpha = 0.12f),
                ),
            ),
        )
        Column(Modifier.fillMaxSize().padding(QuizzTvLayout.screenInset.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                QuizzTvButton(onBack, Modifier.focusRequester(focus), style = QuizzTvButtonStyle.SECONDARY) {
                    Icon(painterResource(UiR.drawable.ic_quizz_back), null, Modifier.size(18.dp))
                    QuizzTvActionLabel(
                        stringResource(ErrorR.string.quizz_error_action_go_back),
                        Modifier.padding(start = 8.dp),
                    )
                }
                Text(
                    stringResource(R.string.quiz_entry_label),
                    Modifier.padding(start = 16.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant,
                )
            }
            Column(
                Modifier.weight(1f).fillMaxWidth(0.6f).verticalScroll(rememberScrollState())
                    .padding(top = QuizzTvLayout.detailHeaderGap.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TvQuizEntryDetails(entry)
                TvQuizRoomAction(entry.available, room, onIntent, onSignIn)
            }
        }
    }
    TvEntryFocus(focus)
}
