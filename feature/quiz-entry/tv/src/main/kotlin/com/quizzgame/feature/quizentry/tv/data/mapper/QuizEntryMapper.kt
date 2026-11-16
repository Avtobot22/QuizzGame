package com.quizzgame.feature.quizentry.tv.data.mapper

import com.quizzgame.core.media.AppearanceMapper
import com.quizzgame.feature.quizentry.tv.data.dto.AvailabilityStatusDto
import com.quizzgame.feature.quizentry.tv.data.dto.QuizEntryDto
import com.quizzgame.feature.quizentry.tv.domain.entity.QuizEntry
import com.quizzgame.shared.catalog.data.mapper.catalogId
import com.quizzgame.shared.catalog.data.mapper.requireCatalog
import com.quizzgame.shared.catalog.data.mapper.toCatalogBadge
import com.quizzgame.shared.catalog.data.mapper.toCatalogMedia
import com.quizzgame.shared.catalog.data.mapper.toQuizUserState

internal fun QuizEntryDto.toDomain(): QuizEntry {
    requireCatalog(title.isNotBlank())
    requireCatalog(stats?.questionCount == null || stats.questionCount > 0)
    requireCatalog(stats?.estimatedDurationSeconds == null || stats.estimatedDurationSeconds > 0)
    primaryAction?.let { requireCatalog(it.title.isNotBlank()) }
    return QuizEntry(
        catalogId(entryId), title, subtitle, description, cover.toCatalogMedia(),
        AppearanceMapper.color(accentColor), badge?.toCatalogBadge(), user.toQuizUserState(),
        availability.status == AvailabilityStatusDto.AVAILABLE, availability.requiresAuthentication,
        stats?.questionCount, stats?.estimatedDurationSeconds,
        primaryAction?.title?.takeIf { availability.status == AvailabilityStatusDto.AVAILABLE },
    )
}
