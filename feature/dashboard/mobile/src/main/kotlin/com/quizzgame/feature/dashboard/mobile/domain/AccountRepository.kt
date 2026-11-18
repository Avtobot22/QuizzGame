package com.quizzgame.feature.dashboard.mobile.domain

import com.quizzgame.feature.dashboard.mobile.domain.entity.Account

internal fun interface AccountRepository { suspend fun current(): Account }
