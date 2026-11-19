plugins {
    alias(libs.plugins.quizz.android.tv.library)
    alias(libs.plugins.quizz.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android { namespace = "com.quizzgame.feature.account.tv" }

dependencies {
    api(projects.core.navigation)
    implementation(projects.core.designsystem.tv)
    implementation(projects.core.presentation)
    implementation(projects.shared.splash)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.serialization.core)
}
