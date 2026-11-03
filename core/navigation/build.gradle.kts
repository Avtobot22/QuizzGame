plugins {
    alias(libs.plugins.quizz.android.infrastructure.library)
    alias(libs.plugins.kotlin.serialization)
}

android { namespace = "com.quizzgame.core.navigation" }

dependencies {
    api(libs.androidx.navigation3.runtime)
    implementation(libs.kotlinx.serialization.core)
    testImplementation(libs.kotlinx.serialization.json)
}
