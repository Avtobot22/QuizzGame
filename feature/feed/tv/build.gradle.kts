plugins {
    alias(libs.plugins.quizz.android.tv.library)
    alias(libs.plugins.quizz.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android { namespace = "com.quizzgame.feature.feed.tv" }

dependencies {
    implementation(libs.androidx.paging.common)
    implementation(libs.androidx.paging.compose)
    testImplementation(libs.androidx.paging.testing)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(projects.core.coroutines)
    implementation(projects.core.presentation)
    implementation(projects.core.featureFlags)
    implementation(projects.core.network)
    implementation(projects.core.session)
    implementation(projects.core.error.uiTv)
    implementation(projects.core.designsystem.tv)
    implementation(projects.core.media)
    implementation(projects.shared.catalog)
    implementation(projects.core.navigation)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    testImplementation(libs.bundles.unit.test)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.retrofit.kotlinx.serialization)
}
