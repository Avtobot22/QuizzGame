plugins {
    alias(libs.plugins.quizz.kotlin.jvm.library)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(projects.core.media)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
}
