plugins {
    alias(libs.plugins.quizz.android.tv.library)
    alias(libs.plugins.quizz.android.hilt)
}

android { namespace = "com.quizzgame.feature.splash.tv" }

dependencies {
    implementation(projects.core.presentation)
    implementation(projects.shared.splash)
    implementation(projects.core.navigation)
    implementation(projects.core.error.uiTv)
    implementation(projects.core.designsystem.tv)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.bundles.unit.test)
}
