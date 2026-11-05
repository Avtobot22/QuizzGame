package com.quizzgame.app.network

import com.quizzgame.core.network.config.NetworkTimeouts
import com.quizzgame.core.network.config.PlatformHeaders
import com.quizzgame.core.network.config.createNetworkJson
import com.quizzgame.core.session.lifecycle.SessionManager
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.core.session.model.TokenPair
import com.quizzgame.core.session.refresh.RefreshRevocationPolicy
import com.quizzgame.core.session.refresh.api.RefreshEndpoint
import com.quizzgame.core.session.storage.SessionRecord
import com.quizzgame.core.session.storage.SessionStore
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class AppNetworkGraphTest {
    @Test
    fun constructionDoesNotMakeHttpCallsAndClosingTheGraphDoesNotOwnTheSession() = runTest {
        val store = object : SessionStore {
            override fun read() = SessionRecord("123e4567-e89b-12d3-a456-426614174000", null)
            override fun write(tokens: TokenPair?): SessionRecord = error("No writes during startup")
            override fun writeRecord(record: SessionRecord): SessionRecord = error("No writes during startup")
        }
        val session = SessionManager.open(store, StandardTestDispatcher(testScheduler))
        val graph = realAppNetworkFactory(settings("https://example.test/"), RefreshRevocationPolicy.never())
            .open(session, createNetworkJson())
        val api = graph.api.retrofit.callFactory() as OkHttpClient
        assertNotSame(Authenticator.NONE, api.authenticator)
        assertEquals(0, api.dispatcher.runningCallsCount())
        assertEquals(0, api.dispatcher.queuedCallsCount())
        graph.close()
        graph.close()
        assertTrue(api.dispatcher.executorService.isShutdown)
        assertEquals(SessionState.Guest, session.state.value)
        session.close()
    }

    @Test
    fun absentOrInsecureRealUrlIsAConfigurationFailure() {
        assertThrows(AppNetworkConfigurationException::class.java) { settings("").realNetworkConfiguration() }
        assertThrows(AppNetworkConfigurationException::class.java) {
            settings("http://example.test/").realNetworkConfiguration()
        }
    }
}

private fun settings(url: String) = AppNetworkSettings(
    url,
    PlatformHeaders("fixture-platform", "Fixture/1"),
    RefreshEndpoint("api/mobile/auth/refresh"),
    NetworkTimeouts(),
    "REAL",
)
