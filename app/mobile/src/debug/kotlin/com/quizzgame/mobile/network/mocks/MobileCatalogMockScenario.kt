package com.quizzgame.mobile.network.mocks

import dev.androidmock.core.request.HeaderEntry
import dev.androidmock.core.response.ResponseSpec
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.seconds

/** Однократные отказы debug-каталога; production не содержит этот класс. */
object MobileCatalogMockScenario {
    enum class Failure { INVALID_CURSOR, EXPIRED, RATE_LIMITED, SERVER, TIMEOUT }
    private val feedLoads = AtomicInteger()
    val feedLoadCount: Int get() = feedLoads.get()
    internal fun recordFeedLoad() { feedLoads.incrementAndGet() }
    private val dashboard = AtomicReference<Failure?>(null)
    private val append = AtomicReference<Failure?>(null)
    fun failDashboard(failure: Failure) { dashboard.set(failure) }
    fun failNextAppend(failure: Failure) { append.set(failure) }
    fun reset() {
        feedLoads.set(0)
        dashboard.set(null)
        append.set(null)
    }
    internal fun dashboardFailure() = response(dashboard.getAndSet(null))
    internal fun appendFailure() = response(append.getAndSet(null))
    private fun response(failure: Failure?): ResponseSpec? = when (failure) {
        Failure.INVALID_CURSOR -> ResponseSpec(status = 400)
        Failure.EXPIRED -> ResponseSpec(status = 410)
        Failure.RATE_LIMITED -> ResponseSpec(status = 429, headers = listOf(HeaderEntry("Retry-After", "1")))
        Failure.SERVER -> ResponseSpec(status = 503)
        Failure.TIMEOUT -> ResponseSpec(status = 504, delay = 60.seconds)
        null -> null
    }
}
