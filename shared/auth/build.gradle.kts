plugins {
    alias(libs.plugins.quizz.android.infrastructure.library)
    alias(libs.plugins.quizz.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android { namespace = "com.quizzgame.shared.auth" }

dependencies {
    api(projects.core.session)
    api(projects.core.error.foundation)
    implementation(projects.core.network)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.bundles.unit.test)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.retrofit.kotlinx.serialization)
}
