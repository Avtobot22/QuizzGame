package com.quizzgame.debug.startup

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quizzgame.app.network.AppNetworkSettings
import com.quizzgame.core.network.auth.AuthPolicy
import com.quizzgame.core.network.auth.RefreshFailureReason
import com.quizzgame.core.network.auth.ReplayPolicy
import com.quizzgame.core.network.auth.RequestPolicy
import com.quizzgame.core.network.config.NetworkTimeouts
import com.quizzgame.core.network.config.PlatformHeaders
import com.quizzgame.core.network.config.createNetworkJson
import com.quizzgame.core.network.result.NetworkFailure
import com.quizzgame.core.network.result.NetworkResult
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.core.session.refresh.RefreshRevocationPolicy
import com.quizzgame.core.session.refresh.api.RefreshEndpoint
import com.quizzgame.core.session.refresh.api.RefreshFailure
import com.quizzgame.debug.network.debugAppNetworkFactory
import dev.androidmock.core.dsl.mockRules
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.http.GET
import retrofit2.http.Tag
import java.io.IOException
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class AppStartupScenariosTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    fun fakeSessionScenariosStayLocalAndUnavailableCanRecover() = runBlocking {
        openDebugSession(context, "GUEST").use { assertEquals(SessionState.Guest, it.state.value) }
        openDebugSession(context, "AUTHENTICATED").use {
            assertTrue(it.state.value is SessionState.Authenticated)
        }
        openDebugSession(context, "UNAVAILABLE").use {
            assertTrue(it.state.value is SessionState.Unavailable)
            it.restoreStoredSession()
            assertEquals(SessionState.Guest, it.state.value)
        }
    }

    @Test
    fun failedMockStartupCanRetryAndClosingTheGraphKeepsItsBorrowedSession() = runBlocking {
        val settings = AppNetworkSettings(
            "",
            PlatformHeaders("fixture-platform", "Fixture/1"),
            RefreshEndpoint("api/fixture/refresh"),
            NetworkTimeouts(),
            "MOCK",
        )
        val factory = debugAppNetworkFactory(
            InstrumentationRegistry.getInstrumentation().context,
            settings,
            "mock/bodies/startup-refresh.json",
            true,
            RefreshRevocationPolicy.never(),
        ) { _, _ -> emptyList() }
        openDebugSession(context, "GUEST").use { session ->
            val json = createNetworkJson()
            assertThrows(IOException::class.java) { factory.open(session, json) }
            factory.open(session, json).use { graph ->
                assertTrue(graph.api.retrofit.baseUrl().host in setOf("127.0.0.1", "localhost", "::1"))
            }
            assertEquals(SessionState.Guest, session.state.value)
        }
    }

    @Test
    fun factoryForwardsTheExplicitPolicyAndInvalidRefreshDoesNotRevokeTokens() = runBlocking {
        val observed = AtomicReference<RefreshFailure>()
        val policy = RefreshRevocationPolicy { failure ->
            observed.set(failure)
            failure == RefreshFailure.Http(401)
        }
        val settings = AppNetworkSettings(
            "",
            PlatformHeaders("fixture-platform", "Fixture/1"),
            RefreshEndpoint("api/fixture/refresh"),
            NetworkTimeouts(),
            "MOCK",
        )
        val factory = debugAppNetworkFactory(
            InstrumentationRegistry.getInstrumentation().context,
            settings,
            "mock/bodies/invalid-refresh.json",
            false,
            policy,
        ) { _, _ ->
            mockRules {
                rule("protected", "/private") { respond { status(401) } }
            }
        }
        openDebugSession(context, "AUTHENTICATED").use { session ->
            val access = session.snapshot().accessToken
            factory.open(session, createNetworkJson()).use { graph ->
                val api = graph.api.retrofit.create(ProtectedApi::class.java)
                assertEquals(
                    NetworkResult.Failure(NetworkFailure.RefreshFailed(RefreshFailureReason.InvalidResponse)),
                    graph.api.results.call {
                        api.load(RequestPolicy(AuthPolicy.REQUIRED, ReplayPolicy.AFTER_REFRESH_ONCE))
                    },
                )
            }
            assertEquals(RefreshFailure.InvalidResponse, observed.get())
            assertTrue(session.state.value is SessionState.Authenticated)
            assertEquals(access, session.snapshot().accessToken)
        }
    }

    interface ProtectedApi {
        @GET("private")
        suspend fun load(@Tag policy: RequestPolicy): JsonObject
    }
}
