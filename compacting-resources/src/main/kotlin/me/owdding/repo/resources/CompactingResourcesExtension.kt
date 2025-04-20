package me.owdding.repo.resources

import me.owdding.repo.resources.types.CompactToArray
import me.owdding.repo.resources.types.CompactToObject
import me.owdding.repo.resources.types.ExternalResource
import me.owdding.repo.resources.types.SubstituteFromDifferentFile

open class CompactingResourcesExtension {
    internal val compactors: MutableList<ResourceType<*>> = mutableListOf()
    internal val externalResources: MutableList<ExternalResource> = mutableListOf()
    var basePath: String? = null

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
}