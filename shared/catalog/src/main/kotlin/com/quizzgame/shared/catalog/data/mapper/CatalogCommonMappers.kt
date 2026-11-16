package com.quizzgame.shared.catalog.data.mapper

import com.quizzgame.core.media.AppearanceMapper
import com.quizzgame.shared.catalog.data.dto.BadgeDto
import com.quizzgame.shared.catalog.data.dto.BadgeVariantDto
import com.quizzgame.shared.catalog.data.dto.PassingStatusDto
import com.quizzgame.shared.catalog.data.dto.UserQuizStateDto
import com.quizzgame.shared.catalog.domain.entity.CatalogBadge
import com.quizzgame.shared.catalog.domain.entity.PassingStatus
import com.quizzgame.shared.catalog.domain.entity.QuizUserState

fun BadgeDto.toCatalogBadge(): CatalogBadge {
    requireCatalog(text.isNotBlank())
    return CatalogBadge(
        text,
        variant == BadgeVariantDto.LIVE,
        AppearanceMapper.color(textColor),
        AppearanceMapper.color(backgroundColor),
    )
}

fun UserQuizStateDto.toQuizUserState(): QuizUserState {
    requireCatalog(progress.isFinite())
    val status = when (passingStatus) {
        PassingStatusDto.NOT_STARTED -> PassingStatus.NOT_STARTED
        PassingStatusDto.IN_PROGRESS -> PassingStatus.IN_PROGRESS
        PassingStatusDto.COMPLETED -> PassingStatus.COMPLETED
    }
    return QuizUserState(bookmarked, progress.coerceIn(0.0, 1.0), status)
}
