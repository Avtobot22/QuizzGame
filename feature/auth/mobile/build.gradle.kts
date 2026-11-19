plugins {
    alias(libs.plugins.quizz.android.mobile.library)
    alias(libs.plugins.quizz.android.hilt)
}

android { namespace = "com.quizzgame.feature.auth.mobile" }

dependencies {
    api(projects.core.navigation)
    implementation(projects.feature.authProvider.mobile)
    implementation(projects.shared.auth)
    implementation(projects.core.designsystem.mobile)
    implementation(projects.core.error.uiMobile)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
