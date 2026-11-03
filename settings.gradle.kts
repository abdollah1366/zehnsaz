pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://maven.tapsell.ir") }
        maven { url = uri("https://jitpack.io") } // Poolakey (Cafe Bazaar billing)
    }
}

rootProject.name = "Zehn chin"
include(":app")
 