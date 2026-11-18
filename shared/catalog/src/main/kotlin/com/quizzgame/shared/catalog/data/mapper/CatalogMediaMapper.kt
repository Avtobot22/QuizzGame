package com.quizzgame.shared.catalog.data.mapper

import com.quizzgame.core.media.AppearanceMapper
import com.quizzgame.core.media.MediaSpec
import com.quizzgame.core.media.data.dto.MediaDto

/** Реестр ресурсов принадлежит каталогу; преобразование медиа не знает предметную область. */
fun MediaDto?.toCatalogMedia(): MediaSpec = AppearanceMapper.media(this, ICON_KEYS, ASSET_KEYS)

private val ICON_KEYS = setOf(
    "navigation.home",
    "navigation.play",
    "navigation.explore",
    "navigation.profile",
    "navigation.library",
    "navigation.settings",
    "action.back",
    "action.play",
    "action.retry",
    "status.live",
    "status.bookmarked",
)
private val ASSET_KEYS = setOf("quiz.cover")
