plugins {
    alias(libs.plugins.quizz.android.tv.library)
    alias(libs.plugins.quizz.android.hilt)
}

android { namespace = "com.quizzgame.feature.auth.tv" }

dependencies {
    api(projects.core.navigation)
    implementation(projects.feature.authProvider.tv)
    implementation(projects.shared.auth)
    implementation(projects.core.designsystem.tv)
    implementation(projects.core.error.uiTv)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
