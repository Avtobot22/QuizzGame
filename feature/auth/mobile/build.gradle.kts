plugins {
    alias(libs.plugins.quizz.android.mobile.library)
    alias(libs.plugins.quizz.android.hilt)
}

val googleClientId = providers.gradleProperty("quizz.auth.googleClientId").orElse("").get()
val yandexLinkHost = providers.gradleProperty("quizz.auth.yandexLinkHost").orElse("").get()

check(googleClientId.isEmpty() || googleClientId.matches(Regex("[A-Za-z0-9-]+[.]apps[.]googleusercontent[.]com")))
check(yandexLinkHost.isEmpty() || yandexLinkHost.matches(Regex("[a-z0-9-]+([.][a-z0-9-]+)+")))

android {
    namespace = "com.quizzgame.feature.auth.mobile"
    buildFeatures.resValues = true
    defaultConfig {
        resValue("string", "auth_google_client_id", googleClientId)
        resValue("string", "auth_yandex_link_host", yandexLinkHost)
    }
}

dependencies {
    api(projects.core.navigation)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(projects.shared.auth)
    implementation(projects.core.presentation)
    implementation(projects.core.designsystem.mobile)
    implementation(projects.core.error.uiMobile)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play)
    implementation(libs.google.identity)
    implementation(libs.google.play.base)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    testImplementation(libs.bundles.unit.test)
}
