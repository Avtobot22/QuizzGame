plugins {
    alias(libs.plugins.quizz.android.mobile.library)
    alias(libs.plugins.quizz.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android { namespace = "com.quizzgame.feature.feed.mobile" }

dependencies {
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(projects.shared.feed)
    implementation(libs.androidx.paging.common)
    implementation(libs.androidx.paging.compose)
    testImplementation(libs.androidx.paging.testing)
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
