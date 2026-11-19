plugins {
    alias(libs.plugins.quizz.android.mobile.library)
    alias(libs.plugins.quizz.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android { namespace = "com.quizzgame.feature.account.mobile" }

dependencies {
    api(projects.core.navigation)
    implementation(projects.core.designsystem.mobile)
    implementation(projects.core.presentation)
    implementation(projects.shared.splash)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.serialization.core)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
