package com.quizzgame.shared.room.presentation

import androidx.annotation.StringRes
import com.quizzgame.core.ui.status.QuizzNoticeTone
import com.quizzgame.core.ui.status.QuizzStatusIcon
import com.quizzgame.shared.room.R
import com.quizzgame.shared.room.domain.entity.result.RoomFailure
import com.quizzgame.shared.room.domain.entity.result.RoomFailureReason
import com.quizzgame.shared.room.domain.entity.session.RoomStreamState

/** Короткое сообщение поверх сохранённого содержимого лобби. */
data class RoomLobbyNotice(
    @StringRes val title: Int,
    val icon: QuizzStatusIcon,
    val tone: QuizzNoticeTone,
    @StringRes val message: Int? = null,
    val retry: Boolean = false,
)

/** Сообщение о восстановлении связи поверх последнего снимка комнаты. */
fun reconnectingNotice(stream: RoomStreamState): RoomLobbyNotice? =
    RoomLobbyNotice(R.string.room_reconnecting, QuizzStatusIcon.SYNC, QuizzNoticeTone.INFORMATION)
        .takeIf { stream is RoomStreamState.Reconnecting }

/** Формирует сообщение, учитывая возможность повторить конкретную команду. */
fun commandFailureNotice(
    stream: RoomStreamState,
    failure: RoomFailure,
    @StringRes title: Int,
    icon: QuizzStatusIcon,
    canRetry: Boolean,
): RoomLobbyNotice {
    val retry = failure.uncertain && canRetry
    val message = when (failure.reason) {
        RoomFailureReason.STATE_CONFLICT, RoomFailureReason.VERSION_CONFLICT -> R.string.room_conflict
        RoomFailureReason.FORBIDDEN -> R.string.room_action_denied
        else -> when {
            stream is RoomStreamState.Reconnecting -> R.string.room_reconnecting
            retry -> R.string.room_command_unconfirmed
            else -> null
        }
    }
    return RoomLobbyNotice(title, icon, QuizzNoticeTone.ERROR, message, retry)
}

/** Хранит ошибку вместе с действием экрана, которое можно повторить. */
data class RoomLobbyCommandError<out Command>(val command: Command, val failure: RoomFailure)
