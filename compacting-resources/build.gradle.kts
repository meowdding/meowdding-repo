import java.util.Properties
import kotlin.io.path.reader

plugins {
    alias(libs.plugins.kotlin.versioned)
    `java-gradle-plugin`
    `maven-publish`
}

val parentProperties = Properties()
layout.projectDirectory.asFile.toPath().resolveSibling("gradle.properties").reader(Charsets.ISO_8859_1).use {
    parentProperties.load(it)
}
version = parentProperties.getProperty("compaction.resources.version")!!

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.gson)
}

gradlePlugin {
    plugins {
        create("meowdding-resources") {
            id = "me.owdding.resources"
            implementationClass = "me.owdding.repo.resources.CompactingResourcesPlugin"
        }
    }
}

publishing {
    publications {
        afterEvaluate {
            named<MavenPublication>("pluginMaven") {
                pom {
                    name.set("Meowdding-Resources")
                    url.set("https://github.com/meowdding/meowdding-repo")

                    scm {
                        connection.set("git:https://github.com/meowdding/meowdding-repo.git")
                        developerConnection.set("git:https://github.com/meowdding/meowdding-repo.git")
                        url.set("https://github.com/meowdding/meowdding-repo")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            name = "TeamResourceful"
            setUrl("https://maven.teamresourceful.com/repository/thatgravyboat/")
            credentials {
                username = System.getenv("MAVEN_USER") ?: providers.gradleProperty("maven_username").orNull
                password = System.getenv("MAVEN_PASS") ?: providers.gradleProperty("maven_password").orNull
            }
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}
