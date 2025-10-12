plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    versionCatalogs.create("libs") {
        from(files("libs.versions.toml"))
    }
}

rootProject.name = "MeowddingRepo"

include("repo")
project(":repo").buildFileName = "../repo.gradle.kts"
include("remote-repo")
includeBuild("compacting-resources")
