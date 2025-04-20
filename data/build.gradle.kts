plugins {
    id("me.owdding.resources")
    id("com.google.devtools.ksp") version "2.1.20-2.0.0"
}

project.layout.buildDirectory.set(rootProject.layout.buildDirectory.dir("meowdding_data"))

tasks {
    compileJava { enabled = false }
}

sourceSets {
    main {
        resources.setSrcDirs(emptyList<Any>())
        java.setSrcDirs(emptyList<Any>())
        kotlin.setSrcDirs(emptyList<Any>())
        kotlin.srcDir(projectDir.resolve("src"))
        resources.srcDir(projectDir)
    }
}

compactingResources {
    basePath = "data"
}

tasks {
    withType<ProcessResources>().configureEach {
        exclude("build.gradle.kts")
        exclude("definitions/**")
        exclude("src/**")
    }
}

dependencies {
    ksp(project(":processor"))
}