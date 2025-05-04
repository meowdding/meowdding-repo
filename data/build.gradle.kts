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
    compactToArray("hotmperks", "hotm")
}

tasks {
    afterEvaluate {
        getByName("kspKotlin") {
            outputs.upToDateWhen { false }
        }
    }

    afterEvaluate {
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

    beforeEvaluate {
        withType<ProcessResources> {
            from(projectDir)
            include { it.path.startsWith("data") }
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
        kotlin.srcDir(projectDir.resolve("src"))
        resources.srcDir(projectDir)
    }
    test {
        resources.setSrcDirs(emptyList<Any>())
        java.setSrcDirs(emptyList<Any>())
        kotlin.setSrcDirs(emptyList<Any>())
    }
}