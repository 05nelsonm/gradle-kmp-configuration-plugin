rootProject.name = "gradle-kmp-configuration-plugin"

pluginManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}

include(":plugin")
includeBuild("build-logic")
