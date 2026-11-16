package com.quizzgame.core.media

import okhttp3.Dns
import okhttp3.OkHttpClient
import java.io.IOException
import java.net.InetAddress
import java.util.concurrent.TimeUnit

fun isolatedImageClient(allowedHosts: Set<String>): OkHttpClient = OkHttpClient.Builder()
    .followRedirects(false)
    .followSslRedirects(false)
    .retryOnConnectionFailure(false)
    .connectTimeout(IMAGE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
    .readTimeout(IMAGE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
    .dns { hostname ->
        Dns.SYSTEM.lookup(hostname).also { addresses ->
            if (addresses.any { !it.isPublicImageAddress() }) throw IOException("Image address rejected")
        }
    }
    .addInterceptor { chain ->
        val request = chain.request()
        if (!request.url.isHttps || request.url.host !in allowedHosts) throw IOException("Image origin rejected")
        if (request.url.username.isNotEmpty() || request.url.password.isNotEmpty()) {
            throw IOException("Image origin rejected")
        }
        chain.proceed(request.newBuilder().headers(okhttp3.Headers.EMPTY).build())
    }
    .build()

internal fun InetAddress.isPublicImageAddress(): Boolean =
    !isAnyLocalAddress && !isLoopbackAddress && !isLinkLocalAddress && !isSiteLocalAddress && !isMulticastAddress &&
        !(address.size == IPV6_BYTES && (address[0].toInt() and IPV6_UNIQUE_MASK) == IPV6_UNIQUE_PREFIX) &&
        !(
            address.size == IPV4_BYTES && (address[0].toInt() and BYTE_MASK) == SHARED_ADDRESS_FIRST &&
                (address[1].toInt() and BYTE_MASK) in SHARED_ADDRESS_RANGE
            )

private const val IMAGE_TIMEOUT_SECONDS = 15L
private const val IPV6_BYTES = 16
private const val IPV4_BYTES = 4
private const val IPV6_UNIQUE_MASK = 0xFE
private const val IPV6_UNIQUE_PREFIX = 0xFC
private const val BYTE_MASK = 0xFF
private const val SHARED_ADDRESS_FIRST = 100
private val SHARED_ADDRESS_RANGE = 64..127
