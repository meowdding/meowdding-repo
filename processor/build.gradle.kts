plugins {
    alias(libs.plugins.kotlin)
}

dependencies {
    implementation(libs.gson)
    implementation(libs.kotlin.std)
    implementation(libs.ksp)

    implementation(libs.bundles.kotlin.poet)
}