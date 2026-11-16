package com.quizzgame.feature.quizentry.tv.domain.entity

import com.quizzgame.core.media.ColorSpec
import com.quizzgame.core.media.MediaSpec
import com.quizzgame.shared.catalog.domain.entity.CatalogBadge
import com.quizzgame.shared.catalog.domain.entity.QuizUserState

internal data class QuizEntry(
    val entryId: String,
    val title: String,
    val subtitle: String? = null,
    val description: String? = null,
    val cover: MediaSpec,
    val accentColor: ColorSpec? = null,
    val badge: CatalogBadge? = null,
    val user: QuizUserState,
    val available: Boolean,
    val requiresAuthentication: Boolean,
    val questionCount: Int? = null,
    val durationSeconds: Int? = null,
    val actionTitle: String? = null,
)
