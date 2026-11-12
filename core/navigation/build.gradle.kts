plugins {
    alias(libs.plugins.quizz.android.shared.library)
    alias(libs.plugins.kotlin.serialization)
}

android { namespace = "com.quizzgame.core.navigation" }

dependencies {
    api(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    testImplementation(libs.kotlinx.serialization.json)
    androidTestImplementation(libs.androidx.navigation3.ui)
}
