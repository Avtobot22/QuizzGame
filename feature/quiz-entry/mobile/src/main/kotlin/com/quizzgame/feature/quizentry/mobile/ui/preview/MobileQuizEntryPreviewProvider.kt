package com.quizzgame.feature.quizentry.mobile.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.error.operation.ErrorOperationContext
import com.quizzgame.core.error.operation.OperationKind
import com.quizzgame.core.error.operation.RetrySafety
import com.quizzgame.core.error.policy.DefaultErrorDecisionPolicy
import com.quizzgame.core.media.MediaSpec
import com.quizzgame.feature.quizentry.mobile.presentation.MobileQuizEntryState
import com.quizzgame.shared.catalog.domain.entity.CatalogBadge
import com.quizzgame.shared.catalog.domain.entity.PassingStatus
import com.quizzgame.shared.catalog.domain.entity.QuizEntry
import com.quizzgame.shared.catalog.domain.entity.QuizUserState

internal class MobileQuizEntryPreviewProvider : PreviewParameterProvider<MobileQuizEntryState> {
    private val entry = QuizEntry(
        entryId = "preview-entry", title = "Кино, сериалы и удивительные истории", subtitle = "Для вечера с друзьями",
        description = "Соберитесь у большого экрана и проверьте, сколько историй вы помните.",
        cover = MediaSpec.Asset("quiz.cover"), badge = CatalogBadge("Новинка", true, null, null),
        user = QuizUserState(false, 0.0, PassingStatus.NOT_STARTED), available = true,
        requiresAuthentication = false, questionCount = 30, durationSeconds = 1200,
    )
    override val values = sequenceOf(
        MobileQuizEntryState.Loading,
        MobileQuizEntryState.Error(
            DefaultErrorDecisionPolicy().decide(
                AppFailure.InvalidServerData,
                ErrorOperationContext(OperationKind.INITIAL_LOAD, false, RetrySafety.SAFE_TO_REPEAT),
            ),
        ),
        MobileQuizEntryState.Content(entry),
        MobileQuizEntryState.Content(entry.copy(available = false)),
        MobileQuizEntryState.Content(entry.copy(requiresAuthentication = true)),
        MobileQuizEntryState.Content(entry.copy(user = QuizUserState(true, 0.45, PassingStatus.IN_PROGRESS))),
        MobileQuizEntryState.Content(entry.copy(user = QuizUserState(true, 1.0, PassingStatus.COMPLETED))),
        MobileQuizEntryState.Content(entry.copy(user = QuizUserState(true, 0.0, PassingStatus.NOT_STARTED))),
    )
}
