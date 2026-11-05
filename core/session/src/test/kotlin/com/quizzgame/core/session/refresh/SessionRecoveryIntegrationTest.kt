package com.quizzgame.core.session.refresh

import com.quizzgame.core.network.auth.CredentialUnavailableException
import com.quizzgame.core.network.auth.RefreshFailedException
import com.quizzgame.core.network.auth.RefreshFailureReason
import com.quizzgame.core.network.result.BackendErrorDetails
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.core.session.storage.SessionIntent
import com.quizzgame.core.session.storage.SessionRecord
import com.quizzgame.core.session.storage.SessionStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.HttpURLConnection.HTTP_OK
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

class SessionRecoveryIntegrationTest : SessionRecoveryTestFixture() {

    @Test
    fun `a late 401 shares the completed refresh failure but a new request may retry`() {
        val lateArrived = gate()
        val releaseLate = gate()
        val refreshCalls = AtomicInteger()
        val fixture = openRecovery(
            object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse = when (request.path) {
                    "/owner" -> {
                        await(lateArrived)
                        MockResponse().setResponseCode(HTTP_UNAUTHORIZED)
                    }
                    "/late" -> {
                        lateArrived.countDown()
                        await(releaseLate)
                        MockResponse().setResponseCode(HTTP_UNAUTHORIZED)
                    }
                    "/new" -> if (request.getHeader("Authorization") == "Bearer $RECOVERY_ACCESS_2") {
                        MockResponse().setBody("ok")
                    } else {
                        MockResponse().setResponseCode(HTTP_UNAUTHORIZED)
                    }
                    RECOVERY_REFRESH_PATH -> if (refreshCalls.incrementAndGet() == 1) {
                        rejectedResponse()
                    } else {
                        pairResponse()
                    }
                    else -> error("Unexpected recovery test request")
                }
            },
        )
        val owner = executeAsync(newCall(fixture, "/owner"))
        val late = executeAsync(newCall(fixture, "/late"))
        val ownerFailure = result(owner).failure
        assertTrue(ownerFailure is RefreshFailedException)
        assertEquals(
            RefreshFailureReason.Http(400, BackendErrorDetails(21008, "Malformed request", null)),
            (ownerFailure as RefreshFailedException).reason,
        )
        releaseLate.countDown()
        val lateFailure = result(late).failure
        assertTrue(lateFailure is RefreshFailedException)
        assertEquals(
            RefreshFailureReason.Http(400, BackendErrorDetails(21008, "Malformed request", null)),
            (lateFailure as RefreshFailedException).reason,
        )
        assertEquals(1, refreshCalls.get())
        assertEquals(2, fixture.store.writes)
        assertEquals(RECOVERY_REFRESH_1, fixture.store.tokens!!.refresh)
        val retry = execute(newCall(fixture, "/new"))
        assertNull(retry.failure)
        assertEquals(HTTP_OK, retry.status)
        assertEquals(2, refreshCalls.get())
        assertEquals(RECOVERY_REFRESH_2, fixture.store.tokens!!.refresh)
    }

    @Test
    fun `a late 401 reuses a completed rotation even when the access string is unchanged`() {
        val lateArrived = gate()
        val releaseLate = gate()
        val ownerCalls = AtomicInteger()
        val lateCalls = AtomicInteger()
        val refreshCalls = AtomicInteger()
        val fixture = openRecovery(
            object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse = when (request.path) {
                    "/owner" -> if (ownerCalls.incrementAndGet() == 1) {
                        await(lateArrived)
                        MockResponse().setResponseCode(HTTP_UNAUTHORIZED)
                    } else {
                        MockResponse().setBody("ok")
                    }
                    "/late" -> if (lateCalls.incrementAndGet() == 1) {
                        lateArrived.countDown()
                        await(releaseLate)
                        MockResponse().setResponseCode(HTTP_UNAUTHORIZED)
                    } else {
                        MockResponse().setBody("ok")
                    }
                    RECOVERY_REFRESH_PATH -> {
                        refreshCalls.incrementAndGet()
                        pairResponse(RECOVERY_ACCESS_1)
                    }
                    else -> error("Unexpected recovery test request")
                }
            },
        )
        val owner = executeAsync(newCall(fixture, "/owner"))
        val late = executeAsync(newCall(fixture, "/late"))
        assertEquals(HTTP_OK, result(owner).status)
        releaseLate.countDown()
        assertEquals(HTTP_OK, result(late).status)
        assertEquals(1, refreshCalls.get())
        assertEquals(2, fixture.store.writes)
        assertEquals(RECOVERY_ACCESS_1, fixture.manager.snapshot().accessToken)
        assertEquals(RECOVERY_REFRESH_2, fixture.store.tokens!!.refresh)
    }

    @Test
    fun `cancelling the owner does not cancel refresh needed by an active waiter`() {
        val refreshArrived = gate()
        val releaseRefresh = gate()
        val waiterArrived = gate()
        val ownerCalls = AtomicInteger()
        val waiterCalls = AtomicInteger()
        val refreshCalls = AtomicInteger()
        val fixture = openRecovery(
            object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse = when (request.path) {
                    "/owner" -> {
                        ownerCalls.incrementAndGet()
                        MockResponse().setResponseCode(HTTP_UNAUTHORIZED)
                    }
                    "/waiter" -> {
                        waiterArrived.countDown()
                        if (waiterCalls.incrementAndGet() == 1) {
                            MockResponse().setResponseCode(HTTP_UNAUTHORIZED)
                        } else {
                            MockResponse().setBody("ok")
                        }
                    }
                    RECOVERY_REFRESH_PATH -> {
                        refreshCalls.incrementAndGet()
                        refreshArrived.countDown()
                        await(releaseRefresh)
                        pairResponse()
                    }
                    else -> error("Unexpected recovery test request")
                }
            },
        )
        val ownerCall = newCall(fixture, "/owner")
        val owner = executeAsync(ownerCall)
        await(refreshArrived)
        val waiter = executeAsync(newCall(fixture, "/waiter"))
        await(waiterArrived)
        ownerCall.cancel()
        val ownerFailure = result(owner).failure
        assertTrue(ownerFailure is IOException || ownerFailure is CancellationException)
        assertFalse(waiter.isDone)
        assertEquals(1, refreshCalls.get())
        releaseRefresh.countDown()
        assertEquals(HTTP_OK, result(waiter).status)
        assertEquals(1, ownerCalls.get())
        assertEquals(2, waiterCalls.get())
        assertEquals(1, refreshCalls.get())
        assertEquals(2, fixture.store.writes)
        assertEquals(RECOVERY_REFRESH_2, fixture.store.tokens!!.refresh)
    }

    @Test
    fun `enqueue and cancellation return while an accepted refresh commit is blocked`() {
        val writeEntered = gate()
        val releaseWrite = gate()
        val fixture = blockedCommitFixture(writeEntered, releaseWrite)
        val previous = fixture.manager.snapshot()
        val identity = fixture.manager.deviceIdProvider.get()
        val ownerCall = newCall(fixture, "/owner")
        val owner = executeAsync(ownerCall)
        await(writeEntered)
        val next = CompletableFuture<HttpResult>()
        val admission = onCaller {
            newCall(fixture, "/next").enqueue(
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        next.complete(HttpResult(null, e))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val status = response.use { it.code }
                        next.complete(HttpResult(status, null))
                    }
                },
            )
        }
        result(admission)
        result(onCaller { ownerCall.cancel() })
        assertNotNull(result(owner).failure)
        assertTrue(result(next).failure is CredentialUnavailableException)
        assertEquals(2, fixture.server.requestCount)
        assertEquals(RECOVERY_REFRESH_1, fixture.store.tokens!!.refresh)
        releaseWrite.countDown()
        runBlocking {
            withTimeout(5_000) { fixture.manager.state.first { it is SessionState.Authenticated } }
        }
        assertEquals(HTTP_OK, execute(newCall(fixture, "/next")).status)
        assertEquals(2, fixture.store.writes)
        assertEquals(RECOVERY_ACCESS_2, fixture.manager.snapshot().accessToken)
        assertEquals(RECOVERY_REFRESH_2, fixture.store.tokens!!.refresh)
        assertEquals(identity, fixture.manager.deviceIdProvider.get())
        assertEquals(previous.sessionGeneration, fixture.manager.snapshot().sessionGeneration)
    }

    private fun blockedCommitFixture(
        writeEntered: CountDownLatch,
        releaseWrite: CountDownLatch,
    ): RecoveryFixture = openRecovery(
        object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = when (request.path) {
                "/owner" -> MockResponse().setResponseCode(HTTP_UNAUTHORIZED)
                "/next" -> {
                    if (request.getHeader("Authorization") == "Bearer $RECOVERY_ACCESS_2") {
                        MockResponse().setBody("ok")
                    } else {
                        MockResponse().setResponseCode(HTTP_UNAUTHORIZED)
                    }
                }
                RECOVERY_REFRESH_PATH -> pairResponse()
                else -> error("Unexpected recovery test request")
            }
        },
        storeDecorator = { backing ->
            object : SessionStore by backing {
                override fun writeRecord(record: SessionRecord): SessionRecord {
                    if (record.intent == SessionIntent.NONE && record.tokens?.access == RECOVERY_ACCESS_2) {
                        writeEntered.countDown()
                        await(releaseWrite)
                    }
                    return backing.writeRecord(record)
                }
            }
        },
    )
}
