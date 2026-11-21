plugins {
    alias(libs.plugins.quizz.android.mobile.library)
    alias(libs.plugins.quizz.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android { namespace = "com.quizzgame.feature.dashboard.mobile" }

dependencies {
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(projects.core.coroutines)
    implementation(projects.core.presentation)
    implementation(projects.core.network)
    implementation(projects.core.session)
    implementation(projects.core.error.uiMobile)
    implementation(projects.core.designsystem.mobile)
    implementation(projects.shared.catalog)
    api(projects.core.navigation)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    testImplementation(libs.bundles.unit.test)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.retrofit.kotlinx.serialization)
}
