plugins {
    alias(libs.plugins.quizz.android.infrastructure.library)
    alias(libs.plugins.quizz.android.hilt)
}

android { namespace = "com.quizzgame.app.foundation" }

dependencies {
    api(projects.core.network)
    api(projects.core.session)
    api(projects.core.error.foundation)
    api(libs.androidx.lifecycle.viewmodel)

    testImplementation(libs.bundles.unit.test)
}
