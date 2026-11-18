package com.quizzgame.feature.auth.mobile.oauth

import android.content.Context
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import android.os.Build
import com.quizzgame.feature.auth.mobile.R
import java.net.URI

object YandexAppLink {
    private const val CALLBACK_PATH = "/auth/yandex"
    private val ATTEMPT_QUERY = Regex("attemptId=([A-Za-z0-9_-]{1,128})")

    fun available(context: Context): Boolean {
        val host = context.getString(R.string.auth_yandex_link_host)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || host.isBlank()) return false
        val manager = context.getSystemService(DomainVerificationManager::class.java)
        val state = manager?.getDomainVerificationUserState(context.packageName)
        return state?.isLinkHandlingAllowed == true &&
            state.hostToStateMap[host] == DomainVerificationUserState.DOMAIN_STATE_VERIFIED
    }

    /** Принимает только HTTPS App Link проверенного домена со своим публичным идентификатором. */
    fun attemptId(context: Context, url: String): String? =
        if (available(context)) parse(context.getString(R.string.auth_yandex_link_host), url) else null

    internal fun parse(host: String, url: String): String? {
        val uri = runCatching { URI(url) }.getOrNull() ?: return null
        if (uri.scheme != "https" || uri.host != host || uri.rawPath != CALLBACK_PATH) return null
        if (uri.port != -1 || uri.rawUserInfo != null || uri.rawFragment != null) return null
        return ATTEMPT_QUERY.matchEntire(uri.rawQuery.orEmpty())?.groupValues?.get(1)
    }
}
