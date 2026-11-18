package com.quizzgame.shared.catalog.data.mapper

import com.quizzgame.core.media.AppearanceMapper
import com.quizzgame.shared.catalog.data.dto.item.FeedItemDto
import com.quizzgame.shared.catalog.data.dto.item.IgnoredFeedItemDto
import com.quizzgame.shared.catalog.data.dto.item.QuizCardDto
import com.quizzgame.shared.catalog.data.dto.pagination.CursorPageInfoDto
import com.quizzgame.shared.catalog.domain.entity.CursorPageInfo
import com.quizzgame.shared.catalog.domain.entity.QuizCard

fun FeedItemDto.toQuizCardOrNull(): QuizCard? = when (this) {
    IgnoredFeedItemDto -> null
    is QuizCardDto -> {
        requireCatalog(title.isNotBlank())
        QuizCard(
            catalogId(id), catalogId(entryId), title, subtitle, image.toCatalogMedia(),
            AppearanceMapper.color(style?.titleColor), AppearanceMapper.color(style?.subtitleColor),
            badge?.toCatalogBadge(), user.toQuizUserState(),
        )
    }
}

fun CursorPageInfoDto.toCursorPage(empty: Boolean): CursorPageInfo {
    requireCatalog(
        if (hasMore) !nextCursor.isNullOrBlank() && nextCursor.length <= MAX_CURSOR_LENGTH else nextCursor == null,
    )
    requireCatalog(!empty || !hasMore)
    return CursorPageInfo(hasMore, nextCursor)
}

private const val MAX_CURSOR_LENGTH = 2048
