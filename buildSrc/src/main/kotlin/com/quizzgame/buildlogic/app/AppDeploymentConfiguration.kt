package com.quizzgame.buildlogic.app

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import java.net.URI
import java.net.URISyntaxException
import java.util.Locale
import java.util.Properties

internal fun Project.deploymentBaseUrl(platform: String): Provider<String> {
    val key = "quizz.$platform.apiBaseUrl"
    val localUrl = providers.fileContents(rootProject.layout.projectDirectory.file("deployment.local.properties"))
        .asText.orElse("").map { content ->
            Properties().apply { content.reader().use(::load) }.getProperty(key, "")
        }
    return providers.gradleProperty(key)
        .orElse(providers.environmentVariable("QUIZZ_${platform.uppercase(Locale.ROOT)}_API_BASE_URL"))
        .orElse(localUrl)
        .map { validateBaseUrl(it) }
}

private fun validateBaseUrl(value: String): String {
    if (value.isBlank()) return ""
    val url = try { URI(value) } catch (_: URISyntaxException) { null }
    require(
        url != null && url.scheme == "https" && url.host != null && url.userInfo == null &&
            url.query == null && url.fragment == null &&
            (url.path.isNullOrEmpty() || url.path.endsWith('/')),
    ) { "API base URL must use HTTPS, end with / and contain no credentials, query or fragment" }
    return value
}
