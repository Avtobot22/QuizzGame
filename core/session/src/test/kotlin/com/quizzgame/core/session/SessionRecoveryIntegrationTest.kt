package com.quizzgame.core.session

import com.quizzgame.core.network.RefreshFailedException
import com.quizzgame.core.network.RefreshFailureReason
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
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
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
                        MockResponse().setResponseCode(HTTP_INTERNAL_ERROR)
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
        assertEquals(RefreshFailureReason.Http(HTTP_INTERNAL_ERROR), (ownerFailure as RefreshFailedException).reason)
        releaseLate.countDown()
        val lateFailure = result(late).failure
        assertTrue(lateFailure is RefreshFailedException)
        assertEquals(RefreshFailureReason.Http(HTTP_INTERNAL_ERROR), (lateFailure as RefreshFailedException).reason)
        assertEquals(1, refreshCalls.get())
        assertEquals(0, fixture.store.writes)
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
        assertEquals(1, fixture.store.writes)
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
        assertEquals(1, fixture.store.writes)
        assertEquals(RECOVERY_REFRESH_2, fixture.store.tokens!!.refresh)
    }

    @Test
    fun `enqueue and cancellation return while an accepted refresh commit is blocked`() {
        val writeEntered = gate()
        val releaseWrite = gate()
        val nextArrived = gate()
        val fixture = blockedCommitFixture(writeEntered, releaseWrite, nextArrived)
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
        await(nextArrived)
        result(onCaller { ownerCall.cancel() })
        assertNotNull(result(owner).failure)
        assertFalse(next.isDone)
        assertEquals(RECOVERY_REFRESH_1, fixture.store.tokens!!.refresh)
        releaseWrite.countDown()
        assertEquals(HTTP_OK, result(next).status)
        assertEquals(1, fixture.store.writes)
        assertEquals(RECOVERY_ACCESS_2, fixture.manager.snapshot().accessToken)
        assertEquals(RECOVERY_REFRESH_2, fixture.store.tokens!!.refresh)
        assertEquals(identity, fixture.manager.deviceIdProvider.get())
        assertEquals(previous.sessionGeneration, fixture.manager.snapshot().sessionGeneration)
    }

    private fun blockedCommitFixture(
        writeEntered: CountDownLatch,
        releaseWrite: CountDownLatch,
        nextArrived: CountDownLatch,
    ): RecoveryFixture = openRecovery(
        object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = when (request.path) {
                "/owner" -> MockResponse().setResponseCode(HTTP_UNAUTHORIZED)
                "/next" -> {
                    nextArrived.countDown()
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
                override fun write(tokens: TokenPair?): SessionRecord {
                    writeEntered.countDown()
                    await(releaseWrite)
                    return backing.write(tokens)
                }
            }
        },
    )
}
