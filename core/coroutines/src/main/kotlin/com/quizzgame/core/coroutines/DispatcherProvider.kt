package com.quizzgame.core.coroutines

import kotlinx.coroutines.CoroutineDispatcher

/** Контексты фоновой работы. В тестах вызывающий код передаёт управляемые dispatchers. */
interface DispatcherProvider {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}
