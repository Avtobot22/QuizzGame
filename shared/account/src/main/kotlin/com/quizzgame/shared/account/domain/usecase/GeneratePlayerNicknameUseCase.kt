package com.quizzgame.shared.account.domain.usecase

import javax.inject.Inject
import kotlin.random.Random

/** Предлагает имя для формы редактирования. Запись выполняется при сохранении. */
class GeneratePlayerNicknameUseCase @Inject constructor() {
    operator fun invoke(): String = "${names.random()}-${Random.nextInt(MIN_SUFFIX, MAX_SUFFIX_EXCLUSIVE)}"

    private companion object {
        val names = listOf("QuizOtter", "QuizFox", "QuizNova", "QuizOrbit", "QuizEcho", "QuizSpark")
        const val MIN_SUFFIX = 100
        const val MAX_SUFFIX_EXCLUSIVE = 1_000
    }
}
