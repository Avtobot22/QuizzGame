package com.quizzgame.core.media.android

import android.content.Context
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.ImageRequest
import com.quizzgame.core.coroutines.DispatcherProvider
import com.quizzgame.core.media.MediaSpec
import com.quizzgame.core.media.isolatedImageClient
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/** Настройка принадлежит окружению приложения; пустой список запрещает сетевые изображения. */
class CatalogMediaLoader(
    context: Context,
    allowedHosts: Set<String>,
    private val dispatchers: DispatcherProvider,
) {
    private val application = context.applicationContext
    private val hosts = allowedHosts.map { it.lowercase() }.toSet()

    fun remoteSource(value: String): String? {
        val url = value.toHttpUrlOrNull() ?: return null
        val originAllowed = url.isHttps && url.host in hosts
        val anonymous = url.username.isEmpty() && url.password.isEmpty()
        return value.takeIf { originAllowed && anonymous }
    }

    suspend fun embeddedSource(media: MediaSpec.Embedded): ByteArray? = withContext(dispatchers.default) {
        media.validatedImageBytes()
    }

    fun close() { loader.shutdown() }

    suspend fun prefetch(urls: List<String>, maxConcurrent: Int) = coroutineScope {
        val semaphore = Semaphore(maxConcurrent.coerceIn(1, MAX_PREFETCH_CONCURRENT))
        urls.mapNotNull(::remoteSource).distinct().take(MAX_PREFETCH_ITEMS).forEach { url ->
            launch { semaphore.withPermit { loader.execute(ImageRequest.Builder(application).data(url).build()) } }
        }
    }

    val loader: ImageLoader = ImageLoader.Builder(context.applicationContext)
        .components { add(OkHttpNetworkFetcherFactory(callFactory = { isolatedImageClient(hosts) })) }
        .build()
}

private const val MAX_PREFETCH_CONCURRENT = 4
private const val MAX_PREFETCH_ITEMS = 32
