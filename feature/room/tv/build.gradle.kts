plugins {
    alias(libs.plugins.quizz.android.tv.library)
    alias(libs.plugins.quizz.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android { namespace = "com.quizzgame.feature.room.tv" }

dependencies {
    api(projects.shared.room)
    implementation(projects.core.designsystem.tv)
    implementation(projects.core.error.uiTv)
    implementation(projects.core.presentation)
    implementation(projects.core.ui)
    implementation(projects.core.navigation)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.zxing.core)
}
