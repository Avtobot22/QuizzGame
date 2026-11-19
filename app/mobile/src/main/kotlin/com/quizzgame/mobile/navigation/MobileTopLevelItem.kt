package com.quizzgame.mobile.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import com.quizzgame.core.media.MediaSpec
import com.quizzgame.shared.catalog.domain.entity.CatalogBadge
import com.quizzgame.core.ui.R as UiR

internal data class MobileTopLevelItem(
    val route: NavKey,
    @param:StringRes val label: Int,
    @param:DrawableRes val icon: Int = UiR.drawable.ic_quizz_room,
    val title: String? = null,
    val media: MediaSpec? = null,
    val badge: CatalogBadge? = null,
)
