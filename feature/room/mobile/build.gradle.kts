plugins {
    alias(libs.plugins.quizz.android.mobile.library)
    alias(libs.plugins.quizz.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android { namespace = "com.quizzgame.feature.room.mobile" }

dependencies {
    api(projects.shared.room)
    implementation(projects.shared.quizEntry)
    implementation(projects.core.designsystem.mobile)
    implementation(projects.core.error.uiMobile)
    implementation(projects.core.presentation)
    implementation(projects.core.ui)
    implementation(projects.core.navigation)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
