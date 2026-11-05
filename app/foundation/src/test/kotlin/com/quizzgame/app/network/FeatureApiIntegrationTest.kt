package com.quizzgame.app.network

import com.quizzgame.core.network.api.ApiCallResult
import com.quizzgame.core.network.auth.AuthPolicy
import com.quizzgame.core.network.auth.RefreshFailureReason
import com.quizzgame.core.network.auth.ReplayPolicy
import com.quizzgame.core.network.auth.RequestPolicy
import com.quizzgame.core.network.config.NetworkTimeouts
import com.quizzgame.core.network.result.NetworkFailure
import com.quizzgame.core.network.result.NetworkResult
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.core.session.model.TokenPair
import com.quizzgame.core.session.refresh.RefreshRevocationPolicy
import com.quizzgame.core.session.refresh.api.RefreshFailure
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.mockwebserver.SocketPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Tag
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class FeatureApiIntegrationTest : ApiExecutorTestFixture() {
    @Test
    fun concurrentFeatureCallsShareOneBootstrapAndUseConfiguredHeadersAndJson() = runBlocking {
        val fixture = fixture()
        fixture.server.enqueue(MockResponse().setBody("""{"value":"first","extra":true}"""))
        fixture.server.enqueue(MockResponse().setBody("""{"value":"second"}"""))
        val gate = CompletableDeferred<Unit>()
        val entered = CompletableDeferred<Unit>()
        val startup = fixture.startup {
            entered.complete(Unit)
            gate.await()
            fixture.session
        }
        val executor = StartupApiCallExecutor(startup)
        val first = async(start = CoroutineStart.UNDISPATCHED) {
            executor.call(Api::class.java) { load(guestPolicy()) }
        }
        val second = async(start = CoroutineStart.UNDISPATCHED) {
            executor.call(Api::class.java) { load(guestPolicy()) }
        }
        entered.await()
        gate.complete(Unit)
        val results = listOf(first.await(), second.await()).map { result ->
            ((result as ApiCallResult.Executed).result as NetworkResult.Success).value["value"]!!.jsonPrimitive.content
        }
        assertEquals(setOf("first", "second"), results.toSet())
        assertEquals(1, fixture.sessionOpens.get())
        assertEquals(1, fixture.graphOpens.get())
        repeat(2) {
            val request = fixture.server.takeRequest(1, TimeUnit.SECONDS)!!
            assertNull(request.getHeader("Authorization"))
            assertEquals("feature-fixture", request.getHeader("X-Client-Platform"))
            assertEquals("FeatureFixture/1", request.getHeader("User-Agent"))
        }
    }

    @Test
    fun requiredGuestCallDoesNotReachHttpAndResponseCallsKeepNetworkErrors() = runBlocking {
        val fixture = fixture()
        val executor = StartupApiCallExecutor(fixture.startup())
        assertEquals(
            ApiCallResult.Executed(NetworkResult.Failure(NetworkFailure.AuthenticationRequired)),
            executor.call(Api::class.java) { load(authPolicy()) },
        )
        assertEquals(0, fixture.server.requestCount)
        fixture.server.enqueue(MockResponse().setResponseCode(503))
        assertEquals(
            ApiCallResult.Executed(NetworkResult.Failure(NetworkFailure.Http(503))),
            executor.response(Api::class.java) { response(guestPolicy()) },
        )
    }

    @Test
    fun aSecondUnauthorizedGetStopsAfterOneIsolatedRefresh() = runBlocking {
        val fixture = fixture(TokenPair("fixture-access-1", "fixture-refresh-1"))
        fixture.server.dispatcher = dispatcher { MockResponse().setBody(REFRESH_PAIR) }
        val result = StartupApiCallExecutor(fixture.startup()).call(Api::class.java) { load(authPolicy()) }
        assertEquals(ApiCallResult.Executed(NetworkResult.Failure(NetworkFailure.Http(401))), result)
        assertEquals(3, fixture.server.requestCount)
        val requests = List(3) { fixture.server.takeRequest(1, TimeUnit.SECONDS)!! }
        assertEquals(listOf("/probe", "/auth/refresh", "/probe"), requests.map { it.path })
        assertNull(requests[1].getHeader("Authorization"))
        assertEquals("fixture-access-2", fixture.session.snapshot().accessToken)
    }

    @Test
    fun onlyTheExplicitTestRevocationStrategyExpiresTheSession() = runBlocking {
        val fixture = fixture(
            TokenPair("fixture-access-1", "fixture-refresh-1"),
            RefreshRevocationPolicy { it == RefreshFailure.Http(401) },
        )
        fixture.server.dispatcher = dispatcher { MockResponse().setResponseCode(401) }
        assertEquals(
            ApiCallResult.Executed(NetworkResult.Failure(NetworkFailure.SessionRevoked)),
            StartupApiCallExecutor(fixture.startup()).call(Api::class.java) { load(authPolicy()) },
        )
        assertEquals(SessionState.Expired, fixture.session.state.value)
        assertNull(fixture.store.tokens)
    }

    @Test
    fun refreshServerFailuresAndTimeoutBlockOldTokensAndKeepTheirOriginalCause() = runBlocking {
        listOf(500, 503, null).forEach { status ->
            val fixture = fixture(
                TokenPair("fixture-access-1", "fixture-refresh-1"),
                timeouts = NetworkTimeouts(read = 100.milliseconds, call = 2.seconds),
            )
            fixture.server.dispatcher = dispatcher {
                status?.let { MockResponse().setResponseCode(it) }
                    ?: MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE)
            }
            val reason = status?.let { RefreshFailureReason.Http(it) } ?: RefreshFailureReason.Timeout
            assertEquals(
                ApiCallResult.Executed(NetworkResult.Failure(NetworkFailure.RefreshUncertain(reason))),
                StartupApiCallExecutor(fixture.startup()).call(Api::class.java) { load(authPolicy()) },
            )
            assertEquals("fixture-access-1", fixture.store.tokens!!.access)
            assertEquals(1, fixture.store.writes)
            assertTrue(fixture.session.state.value is SessionState.Blocked)
        }
    }

    @Test
    fun postTimeoutNeverAutomaticallyReplaysOrRefreshes() = runBlocking {
        val fixture = fixture(
            TokenPair("fixture-access-1", "fixture-refresh-1"),
            timeouts = NetworkTimeouts(read = 100.milliseconds, call = 2.seconds),
        )
        fixture.server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
        assertEquals(
            ApiCallResult.Executed(NetworkResult.Failure(NetworkFailure.Timeout)),
            StartupApiCallExecutor(fixture.startup()).call(Api::class.java) { write(authPolicy()) },
        )
        assertEquals(1, fixture.server.requestCount)
        assertEquals(0, fixture.store.writes)
    }

    interface Api {
        @GET("probe")
        suspend fun load(@Tag policy: RequestPolicy): JsonObject

        @GET("probe")
        suspend fun response(@Tag policy: RequestPolicy): Response<JsonObject>

        @POST("write")
        suspend fun write(@Tag policy: RequestPolicy): JsonObject
    }
}

private fun guestPolicy() = RequestPolicy(AuthPolicy.OPTIONAL, ReplayPolicy.AFTER_REFRESH_ONCE)
private fun authPolicy() = RequestPolicy(AuthPolicy.REQUIRED, ReplayPolicy.AFTER_REFRESH_ONCE)
private const val REFRESH_PAIR = """{"access":"fixture-access-2","refresh":"fixture-refresh-2"}"""

private fun dispatcher(refresh: () -> MockResponse): Dispatcher = object : Dispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse = if (request.path == "/auth/refresh") {
        refresh()
    } else {
        MockResponse().setResponseCode(401)
    }
}
