package com.quizzgame.core.error

/**
 * Разрешённость ручного повтора, которую задаёт владелец операции.
 *
 * SAFE_TO_REPEAT допускает обычный повтор; REQUIRES_CONFIRMATION требует явного подтверждения
 * пользователя через feature, поскольку предыдущая попытка могла выполнить операцию.
 * FORBIDDEN запрещает оба варианта повтора. Безопасность не означает, что повтор устранит причину:
 * постоянный отказ сервера не получает RETRY только из-за SAFE_TO_REPEAT.
 * Этот контракт не меняет сетевую переотправку после refresh токена.
 */
enum class RetrySafety {
    SAFE_TO_REPEAT,
    REQUIRES_CONFIRMATION,
    FORBIDDEN,
}
