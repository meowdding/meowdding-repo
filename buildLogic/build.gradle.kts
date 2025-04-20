import jdk.tools.jlink.resources.plugins

plugins {
    kotlin("jvm") version "2.1.20" apply false
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}