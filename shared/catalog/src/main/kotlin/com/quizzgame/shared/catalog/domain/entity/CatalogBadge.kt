package com.quizzgame.shared.catalog.domain.entity

import com.quizzgame.core.media.ColorSpec

data class CatalogBadge(val text: String, val live: Boolean, val foreground: ColorSpec?, val background: ColorSpec?)
