package com.quizzgame.feature.feed.tv.domain.entity

import com.quizzgame.core.media.ColorSpec
import com.quizzgame.core.media.MediaSpec
import com.quizzgame.shared.catalog.domain.entity.CatalogBadge
import com.quizzgame.shared.catalog.domain.entity.QuizUserState

internal data class Feed(
    val rails: List<QuizRail>,
    val page: CursorPageInfo = CursorPageInfo(false, null),
)

internal data class QuizRail(
    val id: String,
    val title: String? = null,
    val subtitle: String? = null,
    val cards: List<QuizCard>,
    val itemsPage: CursorPageInfo = CursorPageInfo(false, null),
    val snapshotId: Long = 0,
)

internal data class QuizCard(
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
