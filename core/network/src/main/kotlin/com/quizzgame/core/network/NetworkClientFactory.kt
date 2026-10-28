package com.quizzgame.core.network

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

class NetworkClient internal constructor(
    val httpClient: OkHttpClient,
    val retrofit: Retrofit,
)

class NetworkClientFactory(
    private val configuration: NetworkConfiguration,
    private val credentialsProvider: CredentialsProvider = CredentialsProvider.guest(),
) {

    val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = true
        isLenient = false
        coerceInputValues = false
    }

    fun createApiClient(): NetworkClient = create(refresh = false)

    fun createRefreshClient(): NetworkClient = create(refresh = true)

    private fun create(refresh: Boolean): NetworkClient {
        val timeouts = configuration.timeouts
        val client = OkHttpClient.Builder()
            .connectTimeout(timeouts.connect.inWholeMilliseconds, TimeUnit.MILLISECONDS)
            .readTimeout(timeouts.read.inWholeMilliseconds, TimeUnit.MILLISECONDS)
            .writeTimeout(timeouts.write.inWholeMilliseconds, TimeUnit.MILLISECONDS)
            .callTimeout(timeouts.call.inWholeMilliseconds, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(false)
            .followRedirects(false)
            .followSslRedirects(false)
            .addInterceptor(CommonHeadersInterceptor(configuration))
            .addInterceptor(
                if (refresh) AnonymousRequestInterceptor() else AccessTokenInterceptor(credentialsProvider),
            )
            .addInterceptor(RestoreRetryAfterInterceptor())
            .addNetworkInterceptor(PreventServerRetryInterceptor())
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(configuration.baseUrl.value)
            .client(client)
            .addConverterFactory(json.asConverterFactory(HttpHeaderValues.APPLICATION_JSON.toMediaType()))
            .build()
        return NetworkClient(client, retrofit)
    }
}
