plugins {
    kotlin("jvm") version "2.1.20"
    `java-gradle-plugin`
    `maven-publish`
    id("me.owdding.resources") apply false
}

allprojects {
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

gradlePlugin {
    plugins {
        create("meowdding-repo") {
            id = "me.owdding.data-repo"
            implementationClass = "me.owdding.repo.DataRepoPlugin"
        }
    }
}