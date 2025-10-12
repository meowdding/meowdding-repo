import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    id("repo-base")
}

apply(plugin = "org.jetbrains.kotlin.jvm")
tasks.withType<Test> {
    useJUnitPlatform()
}

repositories {
    mavenCentral()
}

dependencies {
    "testImplementation"(kotlin("test"))
}

extensions.getByType<KotlinJvmProjectExtension>().apply {
    jvmToolchain(21)
}

extensions.getByType<JavaPluginExtension>().apply {
    targetCompatibility = JavaVersion.VERSION_21
    sourceCompatibility = JavaVersion.VERSION_21
}
