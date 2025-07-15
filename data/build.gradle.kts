plugins {
    id("me.owdding.resources")
    alias(libs.plugins.ksp)
    `java-gradle-plugin`
}

project.layout.buildDirectory.set(rootProject.layout.buildDirectory.dir("meowdding_data"))

tasks {
    compileJava { enabled = false }
}

compactingResources {
    basePath = "data"
    configureTask(project.tasks.getByName<ProcessResources>("processResources"))
    compactToArray("hotmperks", "hotm")
    compactToArray("hotfperks", "hotf")
}

tasks {
    afterEvaluate {
        getByName("kspKotlin") {
            outputs.upToDateWhen { false }
        }

        listOf(
            "processTestResources",
            "test",
            "testClasses",
            "compileTestJava",
            "compileTestKotlin",
            "pluginUnderTestMetadata",
            "kspTestKotlin"
        ).forEach {
            named(it) {
                enabled = false
            }
        }
    }
}

dependencies {
    ksp(project(":processor"))
    implementation(libs.gson)
}

ksp {
    arg("meowdding.processor.definitions", projectDir.resolve("definitions").toPath().toAbsolutePath().toString())
}

sourceSets {
    main {
        resources.setSrcDirs(emptyList<Any>())
        java.setSrcDirs(emptyList<Any>())
        kotlin.setSrcDirs(emptyList<Any>())
        kotlin.srcDir(projectDir.resolve("src/kotlin"))
        resources.srcDir(projectDir.resolve("resources"))
    }
    test {
        resources.setSrcDirs(emptyList<Any>())
        java.setSrcDirs(emptyList<Any>())
        kotlin.setSrcDirs(emptyList<Any>())
    }
}
