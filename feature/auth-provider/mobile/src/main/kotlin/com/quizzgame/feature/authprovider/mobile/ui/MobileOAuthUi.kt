package com.quizzgame.feature.authprovider.mobile.ui

import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.AuthMethod

class MobileOAuthUi internal constructor(
    val methods: Set<AuthMethod>,
    val status: MobileOAuthStatus,
    val failure: AuthFailure?,
    val start: (AuthMethod) -> Unit,
    val cancel: () -> Unit,
)
