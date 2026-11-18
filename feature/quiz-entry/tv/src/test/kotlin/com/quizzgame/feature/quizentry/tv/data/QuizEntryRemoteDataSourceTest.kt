package com.quizzgame.feature.quizentry.tv.data

import com.quizzgame.core.network.api.ApiCallExecutor
import com.quizzgame.core.network.api.ApiCallResult
import com.quizzgame.core.network.auth.AuthPolicy
import com.quizzgame.core.network.auth.ReplayPolicy
import com.quizzgame.core.network.auth.RequestPolicy
import com.quizzgame.core.network.config.createNetworkJson
import com.quizzgame.core.network.result.NetworkResultAdapter
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class QuizEntryRemoteDataSourceTest {
    @Test
    fun fixtureUsesOptionalAuthAndCorrectEndpoint() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody(fixture()).setHeader("Content-Type", "application/json"))
            var policy: RequestPolicy? = null
            val repository = TvQuizEntryRemoteDataSource(executor(server) { policy = it })
            assertTrue(repository.load("58e2b5b5-05e6-43f5-1580-8e88ec7130fd") is ApiCallResult.Executed)
            val request = server.takeRequest()
            assertEquals("/api/tv/quiz-entries/58e2b5b5-05e6-43f5-1580-8e88ec7130fd", request.path)
            assertNull(request.getHeader("X-Endpoint-Version"))
            assertEquals(RequestPolicy(AuthPolicy.OPTIONAL, ReplayPolicy.AFTER_REFRESH_ONCE), policy)
        }
    }

    private fun executor(server: MockWebServer, observed: (RequestPolicy?) -> Unit = {}): ApiCallExecutor {
        val json = createNetworkJson()
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            observed(chain.request().tag(RequestPolicy::class.java))
            chain.proceed(chain.request())
        }.build()
        val retrofit = Retrofit.Builder().baseUrl(server.url("/")).client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build()
        val adapter = NetworkResultAdapter(json)
        return object : ApiCallExecutor {
            override suspend fun <S : Any, T> call(service: Class<S>, request: suspend S.() -> T): ApiCallResult<T> =
                ApiCallResult.Executed(adapter.call { request(retrofit.create(service)) })

            override suspend fun <S : Any, T> response(
                service: Class<S>,
                request: suspend S.() -> Response<T>,
            ): ApiCallResult<T> = ApiCallResult.Executed(adapter.response { request(retrofit.create(service)) })
        }
    }

    private fun fixture(): String = checkNotNull(javaClass.getResource("/quiz-entry.json")).readText()
}
