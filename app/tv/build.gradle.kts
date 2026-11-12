import com.quizzgame.buildlogic.app.DebugNetworkMode
import com.quizzgame.buildlogic.app.DebugSessionScenario

plugins {
    alias(libs.plugins.quizz.android.tv.application)
    alias(libs.plugins.quizz.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.quizzgame.tv"
    defaultConfig { applicationId = "com.quizzgame.tv" }
}

quizzNetwork {
    clientPlatform.set("android-tv")
    userAgent.set("QuizzGame-TV/0.1")
}

quizzDebug {
    networkMode.set(DebugNetworkMode.MOCK)
    sessionScenario.set(DebugSessionScenario.STORED)
    failNetworkOnce.set(false)
}

dependencies {
    implementation(projects.app.foundation)
    implementation(projects.core.navigation)
    implementation(projects.core.featureFlags)
    implementation(projects.feature.splash.tv)
    implementation(projects.core.network)
    implementation(projects.core.session)
    implementation(projects.core.designsystem.tv)
    implementation(projects.core.error.uiTv)
    implementation(libs.bundles.navigation3)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)

    debugImplementation(projects.app.debugSupport)

    testImplementation(libs.bundles.unit.test)
    testDebugImplementation(libs.junit)
}
