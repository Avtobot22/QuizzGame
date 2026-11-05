package com.quizzgame.core.network.client

import com.quizzgame.core.network.auth.AccessTokenInterceptor
import com.quizzgame.core.network.auth.AnonymousRequestInterceptor
import com.quizzgame.core.network.auth.AuthCallRegistry
import com.quizzgame.core.network.auth.AuthReplayGuardInterceptor
import com.quizzgame.core.network.auth.CredentialsProvider
import com.quizzgame.core.network.auth.RefreshCoordinator
import com.quizzgame.core.network.auth.SessionAuthenticator
import com.quizzgame.core.network.config.NetworkConfiguration
import com.quizzgame.core.network.http.CommonHeadersInterceptor
import com.quizzgame.core.network.http.HttpHeaderValues
import com.quizzgame.core.network.http.PreventServerRetryInterceptor
import com.quizzgame.core.network.http.RestoreRetryAfterInterceptor
import com.quizzgame.core.network.result.NetworkResultAdapter
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

class NetworkClient internal constructor(
    val httpClient: OkHttpClient,
    val retrofit: Retrofit,
    val results: NetworkResultAdapter,
)

class NetworkClientFactory(
    private val configuration: NetworkConfiguration,
    private val json: Json,
    private val credentialsProvider: CredentialsProvider = CredentialsProvider.guest(),
    private val refreshCoordinator: RefreshCoordinator? = null,
) {

    fun createApiClient(): NetworkClient = create(refresh = false)

    fun createRefreshClient(): NetworkClient = create(refresh = true)

    private fun create(refresh: Boolean): NetworkClient {
        val timeouts = configuration.timeouts
        val calls = if (!refresh && refreshCoordinator != null) {
            AuthCallRegistry(credentialsProvider, refreshCoordinator)
        } else {
            null
        }
        val builder = OkHttpClient.Builder()
            .connectTimeout(timeouts.connect.inWholeMilliseconds, TimeUnit.MILLISECONDS)
            .readTimeout(timeouts.read.inWholeMilliseconds, TimeUnit.MILLISECONDS)
            .writeTimeout(timeouts.write.inWholeMilliseconds, TimeUnit.MILLISECONDS)
            .callTimeout(timeouts.call.inWholeMilliseconds, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(false)
            .followRedirects(false)
            .followSslRedirects(false)
            .addInterceptor(CommonHeadersInterceptor(configuration))
            .addInterceptor(
                if (refresh) {
                    AnonymousRequestInterceptor()
                } else {
                    AccessTokenInterceptor(credentialsProvider, calls)
                },
            )
            .addInterceptor(RestoreRetryAfterInterceptor())
            .addNetworkInterceptor(PreventServerRetryInterceptor())
        if (calls != null) {
            builder.eventListenerFactory(calls)
                .authenticator(SessionAuthenticator(credentialsProvider))
                .addNetworkInterceptor(AuthReplayGuardInterceptor(credentialsProvider))
        }
        val client = builder.build()
        val retrofit = Retrofit.Builder()
            .baseUrl(configuration.baseUrl.value)
            .client(client)
            .addConverterFactory(json.asConverterFactory(HttpHeaderValues.APPLICATION_JSON.toMediaType()))
            .build()
        return NetworkClient(client, retrofit, NetworkResultAdapter(json))
    }
}
