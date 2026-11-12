plugins {
    alias(libs.plugins.quizz.android.mobile.library)
    alias(libs.plugins.quizz.android.hilt)
}

android { namespace = "com.quizzgame.feature.splash.mobile" }

dependencies {
    implementation(projects.core.presentation)
    implementation(projects.shared.splash)
    implementation(projects.core.navigation)
    implementation(projects.core.error.uiMobile)
    implementation(projects.core.designsystem.mobile)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.bundles.unit.test)
}
