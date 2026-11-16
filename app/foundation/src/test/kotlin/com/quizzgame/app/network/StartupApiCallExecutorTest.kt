package com.quizzgame.app.network

import com.quizzgame.app.startup.AppStartupCoordinator
import com.quizzgame.core.coroutines.DefaultDispatcherProvider
import com.quizzgame.core.coroutines.DispatcherProvider
import com.quizzgame.core.network.api.ApiCallResult
import com.quizzgame.core.network.api.ApiUnavailableReason
import com.quizzgame.core.network.auth.AuthPolicy
import com.quizzgame.core.network.auth.CredentialUnavailableKind
import com.quizzgame.core.network.auth.RequestPolicy
import com.quizzgame.core.network.config.createNetworkJson
import com.quizzgame.core.network.result.NetworkFailure
import com.quizzgame.core.network.result.NetworkResult
import com.quizzgame.core.session.model.TokenPair
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.SocketPolicy
import okio.Buffer
import okio.BufferedSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.ContinuationInterceptor

class StartupApiCallExecutorTest : ApiExecutorTestFixture() {
    @Test
    fun callAndResponseUseInjectedIoAndReturnToTheCallerDispatcher() = runBlocking {
        val caller = Thread.currentThread()
        Executors.newSingleThreadExecutor { Thread(it, "executor-io") }.asCoroutineDispatcher().use { ioDispatcher ->
            val dispatchers = object : DispatcherProvider {
                override val io = ioDispatcher
                override val default = ioDispatcher
            }
            val executor = StartupApiCallExecutor(fixture().startup(), dispatchers)
            val value = JsonObject(emptyMap())
            val call = executor.call(FeatureApiIntegrationTest.Api::class.java) {
                assertSame(ioDispatcher, currentCoroutineContext()[ContinuationInterceptor])
                value
            }
            assertEquals(ApiCallResult.Executed(NetworkResult.Success(value)), call)
            assertSame(caller, Thread.currentThread())
            val response = executor.response(FeatureApiIntegrationTest.Api::class.java) {
                assertSame(ioDispatcher, currentCoroutineContext()[ContinuationInterceptor])
                Response.success(value)
            }
            assertEquals(ApiCallResult.Executed(NetworkResult.Success(value)), response)
            assertSame(caller, Thread.currentThread())
        }
    }

    @Test
    fun errorBodyIsReadOnInjectedIoAndReleased() = runBlocking {
        Executors.newSingleThreadExecutor { Thread(it, "executor-io") }.asCoroutineDispatcher().use { ioDispatcher ->
            val dispatchers = object : DispatcherProvider {
                override val io = ioDispatcher
                override val default = ioDispatcher
            }
            var requestThread: Thread? = null
            var readThread: Thread? = null
            var closed = false
            val body = object : ResponseBody() {
                override fun contentType() = "application/json".toMediaType()
                override fun contentLength(): Long = -1
                override fun source(): BufferedSource {
                    readThread = Thread.currentThread()
                    return Buffer().writeUtf8("{}")
                }
                override fun close() { closed = true }
            }
            val executor = StartupApiCallExecutor(fixture().startup(), dispatchers)
            val result = executor.response(FeatureApiIntegrationTest.Api::class.java) {
                requestThread = Thread.currentThread()
                Response.error<JsonObject>(500, body)
            }
            assertTrue((result as ApiCallResult.Executed).result is NetworkResult.Failure)
            assertSame(requestThread, readThread)
            assertTrue(closed)
        }
    }

    @Test
    fun infrastructureFailuresStayTypedAndNeverBecomeNetworkConnectionErrors() = runBlocking {
        ApiUnavailableReason.entries.forEach { reason ->
            val fixture = fixture()
            val startup = AppStartupCoordinator(
                json = createNetworkJson(),
                openSession = {
                    if (reason == ApiUnavailableReason.STORAGE) throw IOException("Storage fixture unavailable")
                    fixture.session
                },
                networkFactory = AppNetworkGraphFactory { _, _ ->
                    if (reason == ApiUnavailableReason.CONFIGURATION) throw AppNetworkConfigurationException()
                    throw IOException("Transport fixture unavailable")
                },
                dispatcher = Dispatchers.Default,
            ).also(fixture.startups::add)
            assertEquals(
                ApiCallResult.Unavailable(reason),
                StartupApiCallExecutor(
                    startup,
                    DefaultDispatcherProvider(),
                ).call(FeatureApiIntegrationTest.Api::class.java) {
                    error("An unavailable runtime must not execute the feature request")
                },
            )
            assertEquals(0, fixture.server.requestCount)
        }
    }

    @Test
    fun unavailableCredentialsBlockProtectedCallsWhilePublicHttpStaysAvailable() = runBlocking {
        val fixture = fixture(TokenPair("fixture-access", "fixture-refresh"))
        val startup = fixture.startup()
        startup.ensureStarted()
        fixture.store.readFailure = true
        fixture.session.restoreStoredSession()
        val executor = StartupApiCallExecutor(startup, DefaultDispatcherProvider())
        val protectedResult = executor.call(FeatureApiIntegrationTest.Api::class.java) {
            load(RequestPolicy(AuthPolicy.REQUIRED))
        }
        assertEquals(
            ApiCallResult.Executed(
                NetworkResult.Failure(
                    NetworkFailure.CredentialsUnavailable(CredentialUnavailableKind.STORAGE_UNAVAILABLE),
                ),
            ),
            protectedResult,
        )
        fixture.server.enqueue(MockResponse().setBody("{}"))
        assertTrue(
            executor.call(FeatureApiIntegrationTest.Api::class.java) {
                load(RequestPolicy(AuthPolicy.NONE))
            } is ApiCallResult.Executed,
        )

        assertEquals("fixture-access", fixture.store.tokens!!.access)
        assertEquals(1, fixture.server.requestCount)
    }

    @Test
    fun cancellationOfTheRepositoryCoroutineCancelsTheRetrofitCall() = runBlocking {
        val fixture = fixture()
        fixture.server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
        val executor = StartupApiCallExecutor(fixture.startup(), DefaultDispatcherProvider())
        val pending = launch {
            executor.call(FeatureApiIntegrationTest.Api::class.java) { load(RequestPolicy(AuthPolicy.OPTIONAL)) }
        }
        withContext(Dispatchers.IO) { checkNotNull(fixture.server.takeRequest(5, TimeUnit.SECONDS)) }
        val client = fixture.graph.api.retrofit.callFactory() as OkHttpClient
        val call = client.dispatcher.runningCalls().single()
        pending.cancelAndJoin()
        assertTrue(call.isCanceled())
    }

    @Test
    fun shutdownDuringARequestAndFurtherCallsReturnClosed() = runBlocking {
        val fixture = fixture()
        fixture.server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
        val startup = fixture.startup()
        val executor = StartupApiCallExecutor(startup, DefaultDispatcherProvider())
        val pending = async(start = CoroutineStart.UNDISPATCHED) {
            executor.call(FeatureApiIntegrationTest.Api::class.java) { load(RequestPolicy(AuthPolicy.OPTIONAL)) }
        }
        withContext(Dispatchers.IO) { checkNotNull(fixture.server.takeRequest(5, TimeUnit.SECONDS)) }
        startup.shutdown()
        assertEquals(ApiCallResult.Closed, pending.await())
        assertEquals(
            ApiCallResult.Closed,
            executor.call(FeatureApiIntegrationTest.Api::class.java) { error("Closed runtime cannot run a request") },
        )
    }

    @Test
    fun programmingExceptionsInTheFeatureRequestStillPropagate() = runBlocking {
        val fixture = fixture()
        val unexpected = IllegalStateException("Feature fixture bug")
        val failure = runCatching {
            StartupApiCallExecutor(
                fixture.startup(),
                DefaultDispatcherProvider(),
            ).call(FeatureApiIntegrationTest.Api::class.java) {
                throw unexpected
            }
        }.exceptionOrNull()
        assertTrue(failure is IllegalStateException)
        assertEquals(unexpected.message, failure?.message)
        assertSame(unexpected, generateSequence(failure) { it.cause }.last())
    }
}
