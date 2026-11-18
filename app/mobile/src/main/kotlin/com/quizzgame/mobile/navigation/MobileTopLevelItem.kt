package com.quizzgame.mobile.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import com.quizzgame.core.media.MediaSpec
import com.quizzgame.mobile.R
import com.quizzgame.shared.catalog.domain.entity.CatalogBadge

internal data class MobileTopLevelItem(
    val route: NavKey,
    @param:StringRes val label: Int,
    @param:DrawableRes val icon: Int = R.drawable.ic_room,
    val title: String? = null,
    val media: MediaSpec? = null,
    val badge: CatalogBadge? = null,
)
