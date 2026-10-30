package com.quizzgame.tv.network

import com.quizzgame.core.network.BackendErrorDetails
import com.quizzgame.core.network.NetworkFailure
import com.quizzgame.core.network.NetworkResult
import com.quizzgame.core.network.createNetworkJson
import com.quizzgame.core.session.api.RefreshException
import com.quizzgame.core.session.api.RefreshFailure
import com.quizzgame.debug.network.DebugNetworkConfiguration
import com.quizzgame.debug.network.DebugNetworkGraph
import com.quizzgame.debug.network.DebugNetworkMode
import dev.androidmock.core.dsl.mockRules
import dev.androidmock.core.response.ResponseBody
import dev.androidmock.core.response.ResponseSpec
import dev.androidmock.core.rule.Rule
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import retrofit2.http.GET
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import java.util.concurrent.atomic.AtomicInteger

class NetworkJsonIntegrationTest : DebugNetworkTestFixture() {

    @Test
    fun suppliedJsonDecodesAPIBodiesAndBackendErrorsOnBothTransports() = runBlocking {
        val json = Json(createNetworkJson()) { isLenient = true }
        val graph = DebugNetworkGraph.open(
            DebugNetworkConfiguration(DebugNetworkMode.MOCK, headers),
            json,
            rules(),
        ).also(graphs::add)
        val api = graph.api.retrofit.create(Api::class.java)
        val body = graph.api.results.call { api.body() } as NetworkResult.Success
        assertEquals("fixture", body.value.getValue("value").jsonPrimitive.content)
        val apiFailure = NetworkFailure.Http(HTTP_INTERNAL_ERROR, BackendErrorDetails(API_REASON_CODE, "api-failure"))
        assertEquals(NetworkResult.Failure(apiFailure), graph.api.results.call { api.error() })
        val refresh = graph.tvRefreshClient()
        val pair = refresh.newCall("fixture-original-refresh", INSTALLATION_ID).execute()
        assertEquals("fixture-access", pair.access)
        assertEquals("fixture-refresh", pair.refresh)
        val failure = assertThrows(RefreshException::class.java) {
            refresh.newCall("fixture-original-refresh", INSTALLATION_ID).execute()
        }
        assertEquals(
            RefreshFailure.Http(HTTP_INTERNAL_ERROR, BackendErrorDetails(REFRESH_REASON_CODE, "refresh-failure")),
            failure.failure,
        )
    }

    private fun rules(): List<Rule> {
        val refreshes = AtomicInteger()
        return mockRules {
            rule("body", "/body") {
                respondWith { _, _ -> jsonResponse("{value:fixture}") }
            }
            rule("error", "/error") {
                respond {
                    status(HTTP_INTERNAL_ERROR)
                    bodyText("{reasonCode:17,debugMessage:api-failure}")
                }
            }
            rule("refresh", "/api/tv/auth/refresh") {
                respondWith { _, _ ->
                    if (refreshes.incrementAndGet() == 1) {
                        jsonResponse("{access:fixture-access,refresh:fixture-refresh}")
                    } else {
                        ResponseSpec(
                            status = HTTP_INTERNAL_ERROR,
                            body = ResponseBody.Bytes("{reasonCode:23,debugMessage:refresh-failure}".toByteArray()),
                        )
                    }
                }
            }
        }
    }

    interface Api {
        @GET("body")
        suspend fun body(): JsonObject

        @GET("error")
        suspend fun error(): JsonObject
    }

    companion object {
        private const val API_REASON_CODE = 17
        private const val REFRESH_REASON_CODE = 23
        private const val INSTALLATION_ID = "e4a508be-bb66-46d3-91f6-7b4cabfc977b"
    }
}
