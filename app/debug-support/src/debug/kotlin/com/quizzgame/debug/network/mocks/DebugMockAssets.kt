package com.quizzgame.debug.network.mocks

import android.content.res.AssetManager
import dev.androidmock.core.dsl.BodyAssetResolver
import java.io.ByteArrayOutputStream
import java.io.InputStream

private const val MAX_FIXTURE_BYTES = 1024 * 1024

class DebugMockAssets(private val open: (String) -> InputStream) : BodyAssetResolver {

    override fun read(path: String): ByteArray = open(path).use { input ->
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            require(output.size() + count <= MAX_FIXTURE_BYTES) { "Mock fixture exceeds the response limit" }
            output.write(buffer, 0, count)
        }
        output.toByteArray()
    }

    companion object {

        fun from(assets: AssetManager): DebugMockAssets = DebugMockAssets { path -> assets.open(path) }
    }
}
