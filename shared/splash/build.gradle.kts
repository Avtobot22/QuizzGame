plugins {
    alias(libs.plugins.quizz.android.infrastructure.library)
    alias(libs.plugins.quizz.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android { namespace = "com.quizzgame.shared.splash" }

dependencies {
    api(projects.core.error.foundation)
    api(projects.core.session)
    implementation(projects.core.featureFlags)
    api(projects.core.network)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.bundles.unit.test)
    testImplementation(libs.okhttp.mockwebserver)
}
