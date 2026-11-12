package com.quizzgame.debug.network.mocks

import android.content.Context
import dev.androidmock.core.request.HeaderEntry
import dev.androidmock.core.response.ResponseBody
import dev.androidmock.core.response.ResponseSpec
import java.io.File
import java.net.HttpURLConnection.HTTP_OK
import java.net.HttpURLConnection.HTTP_UNAVAILABLE
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** Управляет только debug HTTP fixtures; журнал содержит категории запросов без их содержимого. */
object SplashMockScenario {
    enum class Stage { LAUNCH, FLAGS, USER }

    @Volatile private var active: Attempt? = null

    fun initialize(context: Context) {
        if (active == null) {
            active = Attempt(
                modeFile = context.filesDir.resolve("splash-mock-scenario"),
                journalFile = context.filesDir.resolve("splash-mock-journal"),
            )
        }
    }

    fun failOnce(stage: Stage): Attempt = Attempt(stage).also { active = it }

    fun emptyRemoteFlags(): Attempt = Attempt(emptyFlags = true).also { active = it }

    fun reset() { active = null }

    fun response(stage: Stage, body: ByteArray): ResponseSpec =
        active?.response(stage, body) ?: jsonResponse(body)

    class Attempt internal constructor(
        private val failedStage: Stage? = null,
        private val emptyFlags: Boolean = false,
        private val modeFile: File? = null,
        private val journalFile: File? = null,
    ) {
        private val counts = mutableMapOf<Stage, Int>()

        @Synchronized
        fun count(stage: Stage): Int = counts[stage] ?: 0

        @Synchronized
        internal fun response(stage: Stage, body: ByteArray): ResponseSpec {
            val count = count(stage) + 1
            counts[stage] = count
            val mode = modeFile?.takeIf { it.isFile }?.readText()?.trim()
            if (mode != null) journalFile?.appendText("${stage.name}\n")
            val failed = (stage == failedStage && count == 1) || (mode == "FAIL_FLAGS" && stage == Stage.FLAGS)
            val content = if (emptyFlags && stage == Stage.FLAGS) "{\"featureToggles\":[]}".toByteArray() else body
            val previewDelay = if (mode == "PREVIEW_LOADING" && stage == Stage.LAUNCH) 12.seconds else Duration.ZERO
            return if (failed) ResponseSpec(status = HTTP_UNAVAILABLE) else jsonResponse(content, previewDelay)
        }
    }
}

private fun jsonResponse(body: ByteArray, delay: Duration = Duration.ZERO): ResponseSpec = ResponseSpec(
    status = HTTP_OK,
    headers = listOf(HeaderEntry("Content-Type", "application/json; charset=utf-8")),
    body = ResponseBody.Bytes(body),
    delay = delay,
)
