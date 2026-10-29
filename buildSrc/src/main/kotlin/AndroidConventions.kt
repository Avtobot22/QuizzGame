import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

private object AndroidConfig {
    const val COMPILE_SDK = 37
    const val MIN_SDK = 26
    const val JDK = 21
}

private fun CommonExtension.configureAndroid(compose: Boolean = true) {
    compileSdk = AndroidConfig.COMPILE_SDK
    defaultConfig.minSdk = AndroidConfig.MIN_SDK
    buildFeatures.compose = compose
    compileOptions.apply {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    lint.apply {
        warningsAsErrors = true
        informational += "AndroidGradlePluginVersion"
    }
}

private fun Project.configureKotlin() {
    extensions.configure<KotlinAndroidProjectExtension> {
        jvmToolchain(AndroidConfig.JDK)
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }
}

val Project.libs: LibrariesForLibs
    get() = extensions.getByType<LibrariesForLibs>()

private fun Project.configureCompose(configuration: Configuration) {
    dependencies.add(configuration.name, dependencies.platform(libs.androidx.compose.bom))
    dependencies.add(configuration.name, libs.androidx.compose.foundation)
    dependencies.add(configuration.name, libs.androidx.compose.ui)
}

fun Project.configureAndroidApplication() {
    extensions.configure<ApplicationExtension> {
        configureAndroid()
        defaultConfig {
            targetSdk = AndroidConfig.COMPILE_SDK
            versionCode = 1
            versionName = "0.1.0"
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
        buildTypes {
            getByName("debug") {
                applicationIdSuffix = ".debug"
                versionNameSuffix = "-debug"
            }
            getByName("release") {
                isMinifyEnabled = true
                isShrinkResources = true
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            }
        }
        sourceSets.getByName("debug").apply {
            kotlin.directories += rootProject.file("app/debug-network/kotlin").absolutePath
            res.directories += rootProject.file("app/debug-network/res").absolutePath
        }
        sourceSets.getByName("testDebug").resources.directories += "src/debug/assets"
    }
    configureKotlin()
    val implementation = configurations.getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME)
    val androidTestImplementation = configurations.getByName("androidTestImplementation")
    val debugImplementation = configurations.getByName("debugImplementation")
    configureCompose(implementation)
    dependencies.add(implementation.name, libs.androidx.activity.compose)
    dependencies.add(
        androidTestImplementation.name,
        dependencies.platform(libs.androidx.compose.bom),
    )
    dependencies.add(androidTestImplementation.name, libs.androidx.compose.ui.test.junit4)
    dependencies.add(androidTestImplementation.name, libs.androidx.test.junit)
    dependencies.add(androidTestImplementation.name, libs.androidx.test.runner)
    dependencies.add(androidTestImplementation.name, libs.kakao.compose)
    dependencies.add(androidTestImplementation.name, libs.androidx.test.espresso.core)
    dependencies.add(debugImplementation.name, libs.androidx.compose.ui.test.manifest)
}

fun Project.configureAndroidLibrary() {
    extensions.configure<LibraryExtension> { configureAndroid() }
    configureKotlin()
    val api = configurations.getByName(JavaPlugin.API_CONFIGURATION_NAME)
    configureCompose(api)
}

fun Project.configureAndroidInfrastructureLibrary() {
    extensions.configure<LibraryExtension> {
        configureAndroid(compose = false)
        defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    configureKotlin()
    dependencies.add("testImplementation", libs.junit)
    dependencies.add("androidTestImplementation", libs.androidx.test.runner)
    dependencies.add("androidTestImplementation", libs.androidx.test.junit)
}
