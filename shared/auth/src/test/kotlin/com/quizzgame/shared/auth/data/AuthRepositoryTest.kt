package com.quizzgame.shared.auth.data

import com.quizzgame.core.network.api.ApiCallExecutor
import com.quizzgame.core.network.api.ApiCallResult
import com.quizzgame.core.network.auth.AuthPolicy
import com.quizzgame.core.network.auth.ReplayPolicy
import com.quizzgame.core.network.auth.RequestPolicy
import com.quizzgame.core.network.config.createNetworkJson
import com.quizzgame.core.network.result.NetworkResultAdapter
import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.domain.entity.AuthResult
import com.quizzgame.shared.auth.domain.entity.AuthSurface
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

class AuthRepositoryTest {
    @Test
    fun requestsUseSharedPathsLeadingZerosAndAnonymousNeverReplayPolicy() = runBlocking {
        MockWebServer().use { server ->
            val policies = mutableListOf<RequestPolicy?>()
            val repo = AuthRepositoryImpl(executor(server, policies))
            server.enqueue(json("""{"methods":[],"guestCatalogAvailable":false}"""))
            assertEquals(emptyList<AuthMethod>(), (repo.methods(AuthSurface.TV) as AuthResult.Success).value)
            server.enqueue(json("""{"access":"test-access","refresh":"test-refresh"}"""))
            assertTrue(repo.verify("challenge", "012345", DEVICE, "idem") is AuthResult.Success)
            val methods = server.takeRequest()
            val verify = server.takeRequest()
            assertEquals("/api/v2/auth/methods?clientSurface=tv", methods.path)
            assertEquals("/api/v2/auth/otp/challenges/challenge/verify", verify.path)
            assertEquals("GET", methods.method)
            assertEquals("POST", verify.method)
            assertEquals("idem", verify.getHeader("Idempotency-Key"))
            assertNull(verify.getHeader("Authorization"))
            assertEquals("""{"code":"012345","deviceId":"$DEVICE"}""", verify.body.readUtf8())
            assertEquals(List(2) { RequestPolicy(AuthPolicy.NONE, ReplayPolicy.NEVER) }, policies)
        }
    }

    @Test
    fun rejectionExpiryAndRetryAfterUseTypedFailures() = runBlocking {
        MockWebServer().use { server ->
            val repo = AuthRepositoryImpl(executor(server, mutableListOf()))
            val errors = listOf(
                Triple(422, 22001, AuthFailure.InvalidCode),
                Triple(410, 22002, AuthFailure.Expired),
                Triple(429, 22003, AuthFailure.RateLimited(17)),
            )
            for ((status, code, expected) in errors) {
                server.enqueue(
                    json("""{"reasonCode":$code,"debugMessage":"fixture"}""")
                        .setResponseCode(status).setHeader("Retry-After", "17"),
                )
                assertEquals(AuthResult.Failed(expected), repo.verify("challenge", "000000", DEVICE, "idem"))
            }
            assertEquals(3, server.requestCount)
        }
    }

    private fun json(body: String) = MockResponse().setBody(body).setHeader("Content-Type", "application/json")

    private fun executor(server: MockWebServer, policies: MutableList<RequestPolicy?>): ApiCallExecutor {
        val json = createNetworkJson()
        val client = OkHttpClient.Builder().retryOnConnectionFailure(false).addInterceptor { chain ->
            policies += chain.request().tag(RequestPolicy::class.java)
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

    private companion object { const val DEVICE = "8ac4f4b2-f026-49c4-90f2-7782d9384dc4" }
}
