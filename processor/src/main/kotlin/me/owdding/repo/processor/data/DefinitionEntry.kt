package me.owdding.repo.processor.data

import com.google.gson.JsonObject

data class DefinitionEntry(
    val definition: Definition,
    val name: String,
    val contains: List<String>,
    private val depends: List<String>,
) {
    val others: List<String> by lazy {
        definition.definitions.filter { depends.contains(it.name) }.flatMap { it.paths }
    }

    val paths: List<String> by lazy { listOf(others, contains).flatten() }
}

fun JsonObject.parse(definition: Definition): DefinitionEntry {
    val name = this.get("name").asNonEmptyString() ?: throw IllegalStateException("Name required!")
    val contains = this.get("contains").asList { it.asNonEmptyString() }.filterNotNull()
    val depends = this.get("depends").asList { it.asNonEmptyString() }.filterNotNull()

    return DefinitionEntry(definition, name, contains, depends)
}