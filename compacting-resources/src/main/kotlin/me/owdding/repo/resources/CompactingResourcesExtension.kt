package me.owdding.repo.resources

import me.owdding.repo.resources.types.*
import org.gradle.api.tasks.SourceSet

open class CompactingResourcesExtension {
    internal val compactors: MutableList<ResourceType<*>> = mutableListOf()
    internal val externalResources: MutableList<ExternalResource> = mutableListOf()
    var basePath: String? = null
    var sourceSets: MutableList<String> = mutableListOf<String>().apply {
        add(SourceSet.MAIN_SOURCE_SET_NAME)
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

    fun processFile(file: String) {
        compactors.add(FileProcessor(file))
    }
}