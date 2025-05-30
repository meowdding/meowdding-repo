plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    versionCatalogs.create("libs") {
        from(files("libs.versions.toml"))
    }
}

rootProject.name = "MeowddingRepo"

include("processor")
include("data")
includeBuild("compacting-resources")