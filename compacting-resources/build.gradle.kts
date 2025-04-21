plugins {
    alias(libs.plugins.kotlin)
    `java-gradle-plugin`
    `maven-publish`
}

version = libs.versions.compacting.resources.get()

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