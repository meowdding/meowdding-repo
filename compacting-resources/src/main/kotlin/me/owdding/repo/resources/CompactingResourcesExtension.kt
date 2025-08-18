package me.owdding.repo.resources

import me.owdding.repo.resources.types.*
import org.gradle.api.tasks.AbstractCopyTask

open class CompactingResourcesExtension {
    internal val compactors: MutableList<ResourceType<*>> = mutableListOf()
    internal val externalResources: MutableList<ExternalResource> = mutableListOf()
    var basePath: String = "repo"
    internal val tasks: MutableList<AbstractCopyTask> = mutableListOf()

    fun configureTask(task: AbstractCopyTask) {
        task.project.tasks.named("compactResources").configure { compactResources ->
            task.dependsOn(compactResources)
            task.mustRunAfter(compactResources)

            task.with(
                task.project.copySpec { spec ->
                    spec.from(compactResources.outputs)
                    spec.into(basePath)
                },
            )
        }
    }

    fun compactToArray(folder: String, output: String = folder) {
        compactors.add(CompactToArray(folder, output))
    }

    fun compactToObject(folder: String, output: String = folder) {
        compactors.add(CompactToObject(folder, output))
    }

    fun downloadResource(url: String, output: String, json: Boolean = true) {
        externalResources.add(ExternalResource(url, output, json))
    }

    fun substituteFromDifferentFile(folder: String, mainFile: String, output: String = folder) {
        compactors.add(SubstituteFromDifferentFile(folder, mainFile, output))
    }

    fun processFile(file: String, output: String = file) {
        compactors.add(FileProcessor(file, output))
    }

    fun removeComments(file: String, output: String = file) {
        compactors.add(RemoveComments(file, output))
    }
}
