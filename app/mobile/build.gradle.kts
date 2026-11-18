import com.quizzgame.buildlogic.app.DebugNetworkMode
import com.quizzgame.buildlogic.app.DebugSessionScenario

plugins {
    alias(libs.plugins.quizz.android.mobile.application)
    alias(libs.plugins.quizz.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.quizzgame.mobile"
    defaultConfig {
        applicationId = "com.quizzgame.mobile"
        manifestPlaceholders["yandexLinkHost"] =
            providers.gradleProperty("quizz.auth.yandexLinkHost").orElse("disabled.invalid").get()
    }
}

quizzNetwork {
    clientPlatform.set("android-mobile")
    userAgent.set("QuizzGame-Mobile/0.1")
}

quizzDebug {
    networkMode.set(DebugNetworkMode.MOCK)
    sessionScenario.set(DebugSessionScenario.STORED)
    failNetworkOnce.set(false)
}

dependencies {
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(projects.app.foundation)
    implementation(projects.core.navigation)
    implementation(projects.core.featureFlags)
    implementation(projects.feature.splash.mobile)
    implementation(projects.feature.auth.mobile)
    implementation(projects.shared.catalog)
    implementation(projects.feature.dashboard.mobile)
    implementation(projects.feature.play.mobile)
    implementation(projects.feature.feed.mobile)
    implementation(projects.feature.quizEntry.mobile)
    implementation(projects.core.network)
    implementation(projects.core.session)
    implementation(projects.core.designsystem.mobile)
    implementation(projects.core.error.uiMobile)
    implementation(libs.bundles.navigation3)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)

    debugImplementation(projects.app.debugSupport)

    testImplementation(libs.bundles.unit.test)
    testDebugImplementation(libs.junit)
}
