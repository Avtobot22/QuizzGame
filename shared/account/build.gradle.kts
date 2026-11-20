plugins {
    alias(libs.plugins.quizz.android.infrastructure.library)
    alias(libs.plugins.quizz.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android { namespace = "com.quizzgame.shared.account" }

dependencies {
    api(projects.shared.splash)
    api(projects.core.presentation)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.bundles.unit.test)
}
