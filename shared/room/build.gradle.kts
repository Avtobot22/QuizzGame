plugins {
    alias(libs.plugins.quizz.android.infrastructure.library)
    alias(libs.plugins.quizz.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android { namespace = "com.quizzgame.shared.room" }

dependencies {
    api(projects.core.error.foundation)
    api(projects.core.session)
    implementation(projects.core.coroutines)
    api(projects.core.presentation)
    implementation(projects.core.network)
    implementation(projects.shared.account)
    implementation(projects.shared.quizEntry)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.tink.android)
    testImplementation(libs.bundles.unit.test)
    testImplementation(libs.okhttp.mockwebserver)
}
