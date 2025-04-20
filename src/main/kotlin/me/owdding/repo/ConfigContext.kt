package me.owdding.repo

import com.google.gson.JsonElement


data class ConfigContext(
    private val include: Set<String>,
    private val exclude: Set<String>,
    private val all: String,
    private val predicate: (JsonElement) -> Boolean,
) {

    fun isInvalid(jsonElement: JsonElement): Boolean {
        return !predicate(jsonElement)
    }

    private val excludeByDefault = !include.contains(all)
    private val data = mutableMapOf<String, Boolean>().apply {
        putAll(exclude.associateWith { false })
        putAll(include.associateWith { true })
        println("Exclusions: $this")
    }

    fun isExcluded(path: String) = !isIncluded(path)
    fun isIncluded(path: String): Boolean {
        println("Testing $path")
        return data.getOrDefault(path, !excludeByDefault)
    }

}