plugins {
    alias(libs.plugins.quizz.android.infrastructure.library)
    alias(libs.plugins.quizz.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android { namespace = "com.quizzgame.shared.feed" }

dependencies {
    api(projects.shared.catalog)
    api(projects.core.session)
    api(projects.core.error.foundation)
    api(libs.androidx.paging.common)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.bundles.unit.test)
    testImplementation(libs.androidx.paging.testing)
}
