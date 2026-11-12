plugins {
    alias(libs.plugins.quizz.android.infrastructure.library)
    alias(libs.plugins.quizz.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android { namespace = "com.quizzgame.app.foundation" }

dependencies {
    api(projects.core.navigation)
    api(projects.core.network)
    api(projects.core.session)
    api(projects.core.error.foundation)
    api(libs.androidx.lifecycle.viewmodel)
    implementation(libs.kotlinx.serialization.core)

    testImplementation(libs.bundles.unit.test)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.kotlinx.serialization.json)
}
