package com.quizzgame.shared.splash.domain

import com.quizzgame.shared.splash.domain.entity.CurrentUser

/** Возвращает профиль только для действующего поколения сессии. */
fun interface CurrentUserReader { suspend fun current(): CurrentUser? }
