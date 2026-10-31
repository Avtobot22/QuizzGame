pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx\\..*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx\\..*")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "QuizzGame"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":app:mobile")
include(":app:tv")
include(":core:ui")
include(":core:designsystem:mobile")
include(":core:designsystem:tv")
include(":core:network")
include(":core:session")
include(":core:error:foundation")
include(":core:error:ui-common")
include(":core:error:ui-mobile")
include(":core:error:ui-tv")

val mockSdkDirectory = file("external/android-mock-server")
check(mockSdkDirectory.resolve("settings.gradle.kts").isFile) {
    "Initialize the pinned mock SDK: git submodule update --init --recursive"
}
includeBuild(mockSdkDirectory) {
    dependencySubstitution {
        substitute(module("dev.androidmock:mock-core")).using(project(":mock-core"))
        substitute(module("dev.androidmock:mock-okhttp-server")).using(project(":mock-okhttp-server"))
    }
}
