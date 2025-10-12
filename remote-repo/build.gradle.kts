plugins {
    `kotlin-logic`
    `maven-publish`
}

evaluationDependsOn(":repo")

dependencies {
    implementation(libs.gson)
}

tasks.processResources {
    val buildRepo = tasks.getByPath(":repo:buildRepo")
    dependsOn(buildRepo)
    mustRunAfter(buildRepo)
    with(copySpec {
        from(buildRepo.outputs)
        into("repo")
    })
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "remote-repo"
            from(components["java"])

            pom {
                name.set("remote-repo")
                url.set("https://github.com/meowdding/meowdding-repo")

                scm {
                    connection.set("git:https://github.com/meowdding/meowdding-repo.git")
                    developerConnection.set("git:https://github.com/meowdding/meowdding-repo.git")
                    url.set("https://github.com/meowdding/meowdding-repo")
                }
            }
        }
    }
    repositories {
        maven {
            setUrl("https://maven.teamresourceful.com/repository/thatgravyboat/")
            credentials {
                username = System.getenv("MAVEN_USER") ?: providers.gradleProperty("maven_username").orNull
                password = System.getenv("MAVEN_PASS") ?: providers.gradleProperty("maven_password").orNull
            }
        }
    }
}
