plugins {
    alias(libs.plugins.kotlin)
    `java-gradle-plugin`
    `maven-publish`
    id("me.owdding.resources") apply false
}

allprojects {
    version = rootProject.version
    group = rootProject.group
    repositories {
        mavenCentral()
    }

    apply(plugin = "org.jetbrains.kotlin.jvm")
    tasks.withType<Test> {
        useJUnitPlatform()
    }

    dependencies {
        testImplementation(kotlin("test"))
    }

    kotlin {
        jvmToolchain(21)
    }

    java {
        targetCompatibility = JavaVersion.VERSION_21
        sourceCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    implementation(project(":data")) {
        isTransitive = false
    }
    implementation(libs.gson)
}

gradlePlugin {
    plugins {
        create("meowdding-repo") {
            id = "me.owdding.repo"
            implementationClass = "me.owdding.repo.DataRepoPlugin"
        }
    }
}
tasks.withType<Jar> {
    mustRunAfter(project(":data").tasks.named("jar"))
}

publishing {
    publications {
        create<MavenPublication>("data") {
            artifactId = "data"
            from(project(":data").components["java"])
        }

        // Gradle plugin development plugin automatically creates publications
        afterEvaluate {
            named<MavenPublication>("pluginMaven") {
                pom {
                    name.set("Meowdding-Repo")
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