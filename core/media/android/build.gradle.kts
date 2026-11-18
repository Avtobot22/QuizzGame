plugins { alias(libs.plugins.quizz.android.infrastructure.library) }
android { namespace = "com.quizzgame.core.media.android" }
dependencies {
    api(projects.core.media)
    implementation(projects.core.coroutines)
    api(libs.coil.core)
    implementation(libs.coil.network.okhttp)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.okhttp)
}
