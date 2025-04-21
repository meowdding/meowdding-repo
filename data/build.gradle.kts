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

    withType<ProcessResources> {
        inputs.dir(projectDir)

        exclude("build.gradle.kts")
        exclude("definitions/**")
        exclude("src/**")
        include("data/**")
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
        afterEvaluate {
            this@main.resources.srcDir(projectDir)
        }
        resources.setSrcDirs(emptyList<Any>())
        java.setSrcDirs(emptyList<Any>())
        kotlin.setSrcDirs(emptyList<Any>())
        kotlin.srcDir(projectDir.resolve("src"))
    }
    test {
        resources.setSrcDirs(emptyList<Any>())
        java.setSrcDirs(emptyList<Any>())
        kotlin.setSrcDirs(emptyList<Any>())
    }
}