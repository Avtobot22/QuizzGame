package com.quizzgame.feature.quizentry.tv.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.presentation.ErrorCopyKey
import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import com.quizzgame.core.error.presentation.ErrorSurface
import com.quizzgame.core.media.MediaSpec
import com.quizzgame.feature.quizentry.tv.domain.entity.QuizEntry
import com.quizzgame.feature.quizentry.tv.presentation.TvQuizEntryState
import com.quizzgame.shared.catalog.domain.entity.CatalogBadge
import com.quizzgame.shared.catalog.domain.entity.PassingStatus
import com.quizzgame.shared.catalog.domain.entity.QuizUserState

internal class TvQuizEntryPreviewProvider : PreviewParameterProvider<TvQuizEntryState> {
    private val entry = QuizEntry(
        entryId = "preview-quiz",
        title = "Кино и сериалы",
        subtitle = "Для знатоков большого экрана",
        description = "Узнайте любимые фильмы по героям, сюжетам и неожиданным фактам.",
        cover = MediaSpec.Placeholder,
        badge = CatalogBadge("Новинка", true, null, null),
        user = QuizUserState(true, 0.0, PassingStatus.NOT_STARTED),
        available = true,
        requiresAuthentication = false,
        questionCount = 20,
        durationSeconds = 900,
        actionTitle = "Начать игру",
    )

    override val values: Sequence<TvQuizEntryState> = sequenceOf(
        TvQuizEntryState.Loading,
        TvQuizEntryState.Error(
            ErrorPresentationSpec(
                ErrorSurface.BLOCKING,
                ErrorCopyKey.CONNECTION_PROBLEM,
                actions = listOf(ErrorActionKind.RETRY),
            ),
        ),
        TvQuizEntryState.Content(entry),
        TvQuizEntryState.Content(entry.copy(available = false, actionTitle = null, badge = null)),
        TvQuizEntryState.Content(entry.copy(requiresAuthentication = true, actionTitle = "Войти для запуска")),
    )
}
