plugins {
    alias(libs.plugins.quizz.android.infrastructure.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.quizzgame.core.network"
}

dependencies {
    api(libs.okhttp)
    api(libs.retrofit)
    api(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit)
    testImplementation(libs.okhttp.mockwebserver)
}
