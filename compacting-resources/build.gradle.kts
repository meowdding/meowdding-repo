plugins {
    kotlin("jvm") version "2.1.20"
    `java-gradle-plugin`
}

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