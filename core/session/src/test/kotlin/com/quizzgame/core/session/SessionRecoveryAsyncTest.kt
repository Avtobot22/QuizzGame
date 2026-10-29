package com.quizzgame.core.session

import com.quizzgame.core.network.AuthPolicy
import com.quizzgame.core.network.RefreshFailedException
import com.quizzgame.core.network.RefreshFailureReason
import com.quizzgame.core.network.ReplayPolicy
import com.quizzgame.core.network.RequestPolicy
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.http.GET
import retrofit2.http.Tag
import java.io.IOException
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import java.net.HttpURLConnection.HTTP_OK
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class SessionRecoveryAsyncTest : SessionRecoveryTestFixture() {
    @Test
    fun `Retrofit asynchronous GET uses native recovery and closes the successful response`() {
        val done = gate()
        val status = AtomicInteger()
        val fixture = openRecovery(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = when {
                request.path == RECOVERY_REFRESH_PATH -> pairResponse()
                request.getHeader("Authorization") == "Bearer $RECOVERY_ACCESS_2" -> MockResponse().setBody("ok")
                else -> MockResponse().setResponseCode(HTTP_UNAUTHORIZED)
            }
        })
        fixture.api.retrofit.create(RecoveryApi::class.java)
            .get(RequestPolicy(AuthPolicy.REQUIRED, ReplayPolicy.AFTER_REFRESH_ONCE))
            .enqueue(object : retrofit2.Callback<ResponseBody> {
                override fun onResponse(
                    call: retrofit2.Call<ResponseBody>,
                    response: retrofit2.Response<ResponseBody>,
                ) {
                    response.body()?.close()
                    response.errorBody()?.close()
                    status.set(response.code())
                    done.countDown()
                }
                override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) { done.countDown() }
            })
        await(done)
        assertEquals(HTTP_OK, status.get())
        assertEquals(RECOVERY_REFRESH_2, fixture.store.tokens!!.refresh)
    }

    @Test
    fun `queued enqueue calls share the first failure before dispatcher admission and a later action can retry`() {
        val releaseRefresh = gate()
        val refreshes = AtomicInteger()
        val fixture = openRecovery(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = when {
                request.path == RECOVERY_REFRESH_PATH -> {
                    if (refreshes.incrementAndGet() == 1) {
                        await(releaseRefresh)
                        MockResponse().setResponseCode(HTTP_INTERNAL_ERROR)
                    } else {
                        pairResponse()
                    }
                }
                request.getHeader("Authorization") == "Bearer $RECOVERY_ACCESS_2" -> MockResponse().setBody("ok")
                else -> MockResponse().setResponseCode(HTTP_UNAUTHORIZED)
            }
        })
        assertEquals(5, fixture.api.httpClient.dispatcher.maxRequestsPerHost)
        val done = CountDownLatch(10)
        val failures = CopyOnWriteArrayList<IOException>()
        repeat(10) {
            newCall(fixture, "/private").enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    failures += e
                    done.countDown()
                }
                override fun onResponse(call: Call, response: Response) {
                    response.close()
                    done.countDown()
                }
            })
        }
        releaseRefresh.countDown()
        await(done)
        assertEquals(10, failures.size)
        failures.forEach {
            assertTrue(it is RefreshFailedException)
            assertEquals(RefreshFailureReason.Http(HTTP_INTERNAL_ERROR), (it as RefreshFailedException).reason)
        }
        assertEquals(1, refreshes.get())
        assertEquals(0, fixture.store.writes)
        assertEquals(HTTP_OK, execute(newCall(fixture, "/new-action")).status)
        assertEquals(2, refreshes.get())
    }

    @Test
    fun `enqueue owner cancellation completes its callback before shared refresh finishes for the waiter`() {
        val refreshArrived = gate()
        val waiterArrived = gate()
        val releaseRefresh = gate()
        val ownerDone = gate()
        val waiterDone = gate()
        val refreshes = AtomicInteger()
        val fixture = openRecovery(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = when {
                request.path == RECOVERY_REFRESH_PATH -> {
                    refreshes.incrementAndGet()
                    refreshArrived.countDown()
                    await(releaseRefresh)
                    pairResponse()
                }
                request.getHeader("Authorization") == "Bearer $RECOVERY_ACCESS_2" -> MockResponse().setBody("ok")
                else -> {
                    if (request.path == "/waiter") waiterArrived.countDown()
                    MockResponse().setResponseCode(HTTP_UNAUTHORIZED)
                }
            }
        })
        val ownerFailure = AtomicReference<IOException?>()
        val owner = newCall(fixture, "/owner")
        owner.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                ownerFailure.set(e)
                ownerDone.countDown()
            }
            override fun onResponse(call: Call, response: Response) {
                response.close()
                ownerDone.countDown()
            }
        })
        await(refreshArrived)
        val waiterStatus = AtomicInteger()
        newCall(fixture, "/waiter").enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { waiterDone.countDown() }
            override fun onResponse(call: Call, response: Response) {
                response.use { waiterStatus.set(it.code) }
                waiterDone.countDown()
            }
        })
        await(waiterArrived)
        owner.cancel()
        await(ownerDone)
        assertTrue(ownerFailure.get() is IOException)
        assertEquals(1L, waiterDone.count)
        releaseRefresh.countDown()
        await(waiterDone)
        assertEquals(HTTP_OK, waiterStatus.get())
        assertEquals(1, refreshes.get())
        assertEquals(RECOVERY_REFRESH_2, fixture.store.tokens!!.refresh)
    }

    @Test
    fun `cancelling all calls or closing coordinator ends waiting without publishing a pending response`() {
        listOf(false, true).forEach { closeCoordinator ->
            val arrived = gate()
            val release = gate()
            val done = gate()
            val failure = AtomicReference<IOException?>()
            val fixture = openRecovery(object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse =
                    if (request.path == RECOVERY_REFRESH_PATH) {
                        arrived.countDown()
                        await(release)
                        pairResponse()
                    } else {
                        MockResponse().setResponseCode(HTTP_UNAUTHORIZED)
                    }
            })
            val call = newCall(fixture, "/private")
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    failure.set(e)
                    done.countDown()
                }
                override fun onResponse(call: Call, response: Response) {
                    response.close()
                    done.countDown()
                }
            })
            await(arrived)
            if (closeCoordinator) fixture.coordinator.close() else call.cancel()
            await(done)
            assertTrue(failure.get() is IOException)
            fixture.coordinator.close()
            release.countDown()
            assertEquals(0, fixture.store.writes)
            assertEquals(RECOVERY_ACCESS_1, fixture.manager.snapshot().accessToken)
            assertFalse(fixture.refresh.httpClient.dispatcher.executorService.isShutdown)
        }
    }

    interface RecoveryApi {
        @GET("private")
        fun get(@Tag policy: RequestPolicy): retrofit2.Call<ResponseBody>
    }
}
