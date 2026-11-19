package com.quizzgame.core.ui.status

import androidx.annotation.DrawableRes
import com.quizzgame.core.ui.R

/** Смысл декоративной пиктограммы. Доступное описание задаёт сопровождающий текст. */
enum class QuizzStatusIcon(@get:DrawableRes val resource: Int) {
    NETWORK(R.drawable.ic_quizz_status_network),
    SERVER(R.drawable.ic_quizz_status_server),
    AUTH(R.drawable.ic_quizz_status_auth),
    GENERIC(R.drawable.ic_quizz_status_generic),
    ROOM(R.drawable.ic_quizz_room),
    CATALOG(R.drawable.ic_quizz_quiz),
}
