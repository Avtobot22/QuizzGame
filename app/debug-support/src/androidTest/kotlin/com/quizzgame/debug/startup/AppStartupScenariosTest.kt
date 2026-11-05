package com.quizzgame.debug.startup

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quizzgame.app.network.AppNetworkSettings
import com.quizzgame.core.network.config.NetworkTimeouts
import com.quizzgame.core.network.config.PlatformHeaders
import com.quizzgame.core.network.config.createNetworkJson
import com.quizzgame.core.session.SessionState
import com.quizzgame.core.session.api.RefreshEndpoint
import com.quizzgame.debug.network.debugAppNetworkFactory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

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
            PlatformHeaders("fixture-platform", "Fixture/1", 1),
            RefreshEndpoint("api/fixture/refresh"),
            NetworkTimeouts(),
            "MOCK",
        )
        val factory = debugAppNetworkFactory(
            InstrumentationRegistry.getInstrumentation().context,
            settings,
            "mock/bodies/startup-refresh.json",
            true,
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
}
