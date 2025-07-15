package me.owdding.repo.resources


import org.gradle.api.Plugin
import org.gradle.api.Project
import kotlin.io.path.ExperimentalPathApi

val jsonExtensions = arrayOf("json", "jsonc", "json5")

class CompactingResourcesPlugin : Plugin<Project> {

    @OptIn(ExperimentalPathApi::class)
    override fun apply(target: Project) {
        target.extensions.create<CompactingResourcesExtension>("compactingResources")

        target.tasks.register("compactResources", CompactResourcesTask::class.java)
    }
}
