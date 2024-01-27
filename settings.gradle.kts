pluginManagement {
    includeBuild("gradle/build-logic")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

val name = "parrotid"


rootProject.name = "$name-parent"


fun includeNamespaced(vararg paths: String) {
    paths.forEach { path ->
        include("${name}-$path")
        project(":${name}-$path").projectDir = file(path)
    }
}

includeNamespaced("core", "paper", "bukkit")