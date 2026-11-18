package com.quizzgame.shared.catalog.domain.entity

import com.quizzgame.core.media.ColorSpec
import com.quizzgame.core.media.MediaSpec

data class QuizCard(
    val id: String,
    val entryId: String,
    val title: String,
    val subtitle: String? = null,
    val image: MediaSpec,
    val titleColor: ColorSpec? = null,
    val subtitleColor: ColorSpec? = null,
    val badge: CatalogBadge? = null,
    val user: QuizUserState,
)
