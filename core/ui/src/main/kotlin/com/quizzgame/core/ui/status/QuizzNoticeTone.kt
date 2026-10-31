package com.quizzgame.core.ui.status

/** Семантический тон сообщения, независимо от причины или источника данных. */
enum class QuizzNoticeTone { NEUTRAL, INFORMATION, ERROR }

/** Смысл декоративной пиктограммы; доступное описание задаёт сопровождающий текст. */
enum class QuizzStatusIcon { NETWORK, SERVER, AUTH, GENERIC }
