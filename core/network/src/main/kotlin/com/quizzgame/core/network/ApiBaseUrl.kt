package com.quizzgame.core.network

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

class ApiBaseUrl private constructor(val value: HttpUrl) {

    internal fun accepts(url: HttpUrl): Boolean =
        url.scheme == value.scheme && url.host == value.host && url.port == value.port

    companion object {

        fun https(value: String): ApiBaseUrl = create(value, allowLoopback = false)
        fun debugLoopback(value: String): ApiBaseUrl = create(value, allowLoopback = true)

        private fun create(value: String, allowLoopback: Boolean): ApiBaseUrl {
            val url = value.toHttpUrl()
            require(url.username.isEmpty() && url.password.isEmpty()) { "API URL must not contain credentials" }
            require(url.query == null && url.fragment == null) { "API URL must not contain query or fragment" }
            require(url.encodedPath.endsWith('/')) { "API URL must end with /" }
            require(url.isHttps || (allowLoopback && url.host == "127.0.0.1")) {
                "API URL requires HTTPS or an explicitly allowed debug loopback address"
            }
            return ApiBaseUrl(url)
        }
    }
}
