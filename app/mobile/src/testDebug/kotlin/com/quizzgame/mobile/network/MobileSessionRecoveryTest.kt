package com.quizzgame.mobile.network

import com.quizzgame.core.network.AuthPolicy
import com.quizzgame.core.network.PlatformHeaders
import com.quizzgame.core.network.ReplayPolicy
import com.quizzgame.core.network.RequestPolicy
import com.quizzgame.core.session.SessionManager
import com.quizzgame.core.session.SessionRecord
import com.quizzgame.core.session.SessionStore
import com.quizzgame.core.session.TokenPair
import com.quizzgame.core.session.api.RefreshEndpoint
import com.quizzgame.debug.network.DebugNetworkConfiguration
import com.quizzgame.debug.network.DebugNetworkMode
import com.quizzgame.debug.network.mocks.DebugMockAssets
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test
import java.net.HttpURLConnection.HTTP_OK

class MobileSessionRecoveryTest {
    @Test
    fun mobileOptInGraphRecoversThroughItsOwnSDKEndpointAndREALUsesTheSameAuthenticator() = runBlocking {
        val store = MobileRecoveryStore()
        SessionManager.open(store).use { manager ->
            val headers = PlatformHeaders("test-mobile", "mobile-recovery-tests/1", 1)
            openMobileDebugNetwork(
                DebugNetworkConfiguration(DebugNetworkMode.MOCK, headers),
                manager,
                RefreshEndpoint("debug/mobile/auth/refresh"),
                DebugMockAssets { path -> checkNotNull(javaClass.classLoader!!.getResourceAsStream(path)) },
            ).use { graph ->
                val request = Request.Builder()
                    .url(checkNotNull(graph.api.retrofit.baseUrl().resolve("debug/mobile/session")))
                    .tag(RequestPolicy::class.java, RequestPolicy(AuthPolicy.REQUIRED, ReplayPolicy.AFTER_REFRESH_ONCE))
                    .build()
                graph.api.httpClient.newCall(request).execute().use { assertEquals(HTTP_OK, it.code) }
                assertEquals("fixture-mobile-refresh-2", store.record.tokens!!.refresh)
                assertNotSame(Authenticator.NONE, graph.api.httpClient.authenticator)
                assertSame(Authenticator.NONE, graph.refresh.httpClient.authenticator)
                val mockAuthenticatorType = graph.api.httpClient.authenticator.javaClass
                openMobileDebugNetwork(
                    DebugNetworkConfiguration(DebugNetworkMode.REAL, headers, "https://example.invalid/"),
                    manager,
                    RefreshEndpoint("caller/refresh"),
                    bodyAssets = { error("REAL must not read mock assets") },
                ).use { real ->
                    assertEquals(mockAuthenticatorType, real.api.httpClient.authenticator.javaClass)
                    assertSame(Authenticator.NONE, real.refresh.httpClient.authenticator)
                }
            }
            assertEquals("fixture-mobile-access-2", manager.snapshot().accessToken)
        }
    }
}

private class MobileRecoveryStore : SessionStore {
    var record = SessionRecord(
        "f4a508be-bb66-46d3-91f6-7b4cabfc977b",
        TokenPair("fixture-mobile-access-1", "fixture-mobile-refresh-1"),
    )
    override fun read(): SessionRecord = record
    override fun write(tokens: TokenPair?): SessionRecord = SessionRecord(record.deviceId, tokens).also { record = it }
}
