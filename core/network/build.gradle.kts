plugins {
    alias(libs.plugins.quizz.android.infrastructure.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.quizzgame.core.network"
}

dependencies {
    api(libs.bundles.network.api)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.bundles.network.test)
    testImplementation(libs.junit)
}
