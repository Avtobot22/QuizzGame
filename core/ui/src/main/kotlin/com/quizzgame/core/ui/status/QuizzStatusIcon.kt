package com.quizzgame.core.ui.status

import androidx.annotation.DrawableRes
import com.quizzgame.core.ui.R

/** Смысл декоративной пиктограммы. Доступное описание задаёт сопровождающий текст. */
enum class QuizzStatusIcon(@get:DrawableRes val resource: Int) {
    NETWORK(R.drawable.ic_quizz_status_network),
    SYNC(R.drawable.ic_quizz_refresh),
    CLOCK(R.drawable.ic_quizz_clock),
    CHECK(R.drawable.ic_quizz_check),
    PEOPLE(R.drawable.ic_quizz_profile),
    REMOVE_PLAYER(R.drawable.ic_quizz_remove_player),
    EXIT(R.drawable.ic_quizz_logout),
    SERVER(R.drawable.ic_quizz_status_server),
    AUTH(R.drawable.ic_quizz_status_auth),
    GENERIC(R.drawable.ic_quizz_status_generic),
    ROOM(R.drawable.ic_quizz_room),
    CATALOG(R.drawable.ic_quizz_quiz),
}
