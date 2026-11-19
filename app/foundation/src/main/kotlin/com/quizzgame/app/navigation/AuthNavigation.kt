package com.quizzgame.app.navigation

import java.util.UUID

data class AuthNavigation(
    val pending: PendingAuthReturn? = null,
    val begin: (AuthReturnTarget) -> Unit = {},
    val cancel: () -> Unit = {},
    val acknowledge: (UUID) -> Unit = {},
)
