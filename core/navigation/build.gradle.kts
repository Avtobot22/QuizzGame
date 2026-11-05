plugins {
    alias(libs.plugins.quizz.android.shared.library)
    alias(libs.plugins.kotlin.serialization)
}

android { namespace = "com.quizzgame.core.navigation" }

dependencies {
    api(projects.core.session)
    api(libs.androidx.navigation3.runtime)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.androidx.lifecycle.runtime.compose)
    testImplementation(libs.kotlinx.serialization.json)
}
