package com.quizzgame.tv.network.mocks

import dev.androidmock.core.request.HeaderEntry
import dev.androidmock.core.response.ResponseSpec
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.seconds

/** Однократный сбой следующего append, доступный только в debug. */
object FeedMockScenario {
    enum class Failure { INVALID_CURSOR, EXPIRED, RATE_LIMITED, SERVER, TIMEOUT }
    private val pending = AtomicReference<Failure?>(null)

    fun failNextAppend(failure: Failure) { pending.set(failure) }
    fun reset() { pending.set(null) }

    internal fun nextResponse(): ResponseSpec? = when (pending.getAndSet(null)) {
        Failure.INVALID_CURSOR -> ResponseSpec(status = 400)
        Failure.EXPIRED -> ResponseSpec(status = 410)
        Failure.RATE_LIMITED -> ResponseSpec(status = 429, headers = listOf(HeaderEntry("Retry-After", "1")))
        Failure.SERVER -> ResponseSpec(status = 500)
        Failure.TIMEOUT -> ResponseSpec(status = 504, delay = 60.seconds)
        null -> null
    }
}
