plugins {
    kotlin("jvm") version "2.1.20"
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
    implementation(project(":data"))!!
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
    }
}