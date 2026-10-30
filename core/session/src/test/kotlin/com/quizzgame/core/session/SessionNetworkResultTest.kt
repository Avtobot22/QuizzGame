package com.quizzgame.core.session

import com.quizzgame.core.network.AuthPolicy
import com.quizzgame.core.network.BackendErrorDetails
import com.quizzgame.core.network.NetworkFailure
import com.quizzgame.core.network.NetworkResult
import com.quizzgame.core.network.NetworkTimeouts
import com.quizzgame.core.network.RefreshFailureReason
import com.quizzgame.core.network.ReplayPolicy
import com.quizzgame.core.network.RequestPolicy
import com.quizzgame.core.session.api.RefreshFailure
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.mockwebserver.SocketPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.http.GET
import retrofit2.http.Tag
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class SessionNetworkResultTest : SessionRecoveryTestFixture() {

    @Test
    fun `refresh HTTP errors preserve backend details instead of returning the original GET 401`() = runBlocking {
        val fixture = openRecovery(
            dispatcher {
                MockResponse().setResponseCode(500).setBody(
                    """{"reasonCode":17,"debugMessage":"private-refresh","userFriendlyMessage":null}""",
                )
            },
        )
        val result = request(fixture)
        assertEquals(
            NetworkResult.Failure(
                NetworkFailure.RefreshFailed(
                    RefreshFailureReason.Http(500, BackendErrorDetails(17, "private-refresh", null)),
                ),
            ),
            result,
        )
        assertFalse(result.toString().contains("private-refresh"))
        assertEquals(RECOVERY_ACCESS_1, fixture.manager.snapshot().accessToken)
        assertEquals(0, fixture.store.writes)
        assertEquals(2, fixture.server.requestCount)
    }

    @Test
    fun `refresh malformed JSON and missing tokens retain invalid response cause and session`() = runBlocking {
        listOf("{", "{}", """{"access":" ","refresh":"fixture"}""").forEach { body ->
            val fixture = openRecovery(dispatcher { MockResponse().setBody(body) })
            assertEquals(
                NetworkResult.Failure(NetworkFailure.RefreshFailed(RefreshFailureReason.InvalidResponse)),
                request(fixture),
            )
            assertEquals(RECOVERY_ACCESS_1, fixture.manager.snapshot().accessToken)
            assertEquals(0, fixture.store.writes)
        }
    }

    @Test
    fun `refresh timeout is distinct and does not revoke the session`() = runBlocking {
        val fixture = openRecovery(
            dispatcher { MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE) },
            timeouts = NetworkTimeouts(read = 100.milliseconds, call = 2.seconds),
        )
        assertEquals(
            NetworkResult.Failure(NetworkFailure.RefreshFailed(RefreshFailureReason.Timeout)),
            request(fixture),
        )
        assertEquals(RECOVERY_ACCESS_1, fixture.manager.snapshot().accessToken)
        assertEquals(0, fixture.store.writes)
    }

    @Test
    fun `configured revocation and second GET unauthorized have different results`() = runBlocking {
        val revoked = openRecovery(
            dispatcher { MockResponse().setResponseCode(401) },
            revocationPolicy = RefreshRevocationPolicy { failure -> failure == RefreshFailure.Http(401) },
        )
        assertEquals(NetworkResult.Failure(NetworkFailure.SessionRevoked), request(revoked))
        assertTrue(revoked.manager.state.value is SessionState.Expired)
        val repeated = openRecovery(dispatcher { pairResponse() })
        assertEquals(NetworkResult.Failure(NetworkFailure.Http(401)), request(repeated))
        assertEquals(RECOVERY_ACCESS_2, repeated.manager.snapshot().accessToken)
        assertEquals(3, repeated.server.requestCount)
    }

    @Test
    fun `unexpected refresh policy exception propagates instead of becoming IO`() {
        val unexpected = IllegalStateException("fixture policy failure")
        val fixture = openRecovery(
            dispatcher { MockResponse().setResponseCode(500) },
            revocationPolicy = RefreshRevocationPolicy { throw unexpected },
        )
        val actual = assertThrows(IllegalStateException::class.java) {
            runBlocking { request(fixture) }
        }
        assertSame(unexpected, actual)
        assertEquals(RECOVERY_ACCESS_1, fixture.manager.snapshot().accessToken)
    }

    private fun dispatcher(refresh: () -> MockResponse): Dispatcher = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse = if (request.path == RECOVERY_REFRESH_PATH) {
            refresh()
        } else {
            MockResponse().setResponseCode(401)
        }
    }

    private suspend fun request(fixture: RecoveryFixture): NetworkResult<JsonObject> {
        val api = fixture.api.retrofit.create(Api::class.java)
        return fixture.api.results.call {
            api.get(RequestPolicy(AuthPolicy.REQUIRED, ReplayPolicy.AFTER_REFRESH_ONCE))
        }
    }

    interface Api {
        @GET("private")
        suspend fun get(@Tag policy: RequestPolicy): JsonObject
    }
}
