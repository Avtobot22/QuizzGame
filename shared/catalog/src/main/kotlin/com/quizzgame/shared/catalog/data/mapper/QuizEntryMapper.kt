package com.quizzgame.shared.catalog.data.mapper

import com.quizzgame.core.media.AppearanceMapper
import com.quizzgame.shared.catalog.data.dto.entry.AvailabilityStatusDto
import com.quizzgame.shared.catalog.data.dto.entry.QuizEntryDto
import com.quizzgame.shared.catalog.domain.entity.QuizEntry

fun QuizEntryDto.toDomain(): QuizEntry {
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
