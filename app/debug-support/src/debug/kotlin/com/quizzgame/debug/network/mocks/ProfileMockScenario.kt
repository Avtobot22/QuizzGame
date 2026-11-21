package com.quizzgame.debug.network.mocks

import com.quizzgame.shared.account.domain.entity.PlayerNickname
import dev.androidmock.core.dsl.BodyAssetResolver
import dev.androidmock.core.dsl.RulesDsl
import dev.androidmock.core.request.RequestBodySnapshot
import dev.androidmock.core.request.RequestSnapshot
import dev.androidmock.core.response.ResponseSpec
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import java.net.HttpURLConnection.HTTP_BAD_REQUEST
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import java.util.concurrent.ConcurrentHashMap

/** Данные профиля встроенного debug-сервера. */
object ProfileMockScenario {
    private val names = ConcurrentHashMap<String, String>()

    fun reset() { names.clear() }

    internal fun save(id: String, nickname: String) { names[id] = nickname }

    internal fun updated(profile: JsonObject): JsonObject {
        val id = profile["id"].toString()
        val name = names[id] ?: return profile
        val info = profile.getValue("profile").jsonObject
        return JsonObject(profile + ("profile" to JsonObject(info + ("displayName" to JsonPrimitive(name)))))
    }
}

fun RulesDsl.profileMockRules(assets: BodyAssetResolver, json: Json, surface: String) {
    val path = "/api/$surface/user"
    val readProfile = { json.parseToJsonElement(assets.read("mock/bodies/user.json").decodeToString()).jsonObject }
    rule("splash-user", path) {
        match { method("GET") }
        respondWith { _, _ ->
            val profile = ProfileMockScenario.updated(readProfile())
            SplashMockScenario.response(SplashMockScenario.Stage.USER, profile.toString().encodeToByteArray())
        }
    }
    rule("profile-nickname", path) {
        match { method("PATCH") }
        respondWith { request, _ ->
            val profile = readProfile()
            updateMockNickname(json, request, profile)
        }
    }
}

private fun updateMockNickname(json: Json, request: RequestSnapshot, profile: JsonObject): ResponseSpec {
    val authorization = request.headers.values("Authorization").singleOrNull()
    if (authorization?.startsWith("Bearer ") != true) return authError(HTTP_UNAUTHORIZED to 0)
    val input = nicknameField(json, request)
    val name = input?.let(PlayerNickname::normalize)
    return when {
        input == null -> authError(HTTP_BAD_REQUEST to 0)
        name == null -> authError(INVALID_NICKNAME)
        else -> {
            ProfileMockScenario.save(profile["id"].toString(), name)
            authJson(ProfileMockScenario.updated(profile).toString())
        }
    }
}

private fun nicknameField(json: Json, request: RequestSnapshot): String? {
    val body = request.body as? RequestBodySnapshot.Buffered ?: return null
    val payload = runCatching { json.parseToJsonElement(body.bytes.decodeToString()).jsonObject }.getOrNull()
        ?: return null
    if (payload.size != 1) return null
    return (payload["displayName"] as? JsonPrimitive)?.takeIf { it.isString }?.content
}

private val INVALID_NICKNAME = 422 to 33001
