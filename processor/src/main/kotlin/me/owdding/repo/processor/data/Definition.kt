package me.owdding.repo.processor.data

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.squareup.kotlinpoet.ClassName
import java.nio.file.Path
import kotlin.io.path.nameWithoutExtension


data class Definition(val name: String, val definitions: MutableList<DefinitionEntry>) {
    fun getClassName(): String = "${name.replaceFirstChar { it.uppercase() }}Extension"
    fun toClassName() = ClassName("me.owdding.repo.extensions.generated", getClassName())
}

fun JsonArray.parse(path: Path): Definition {
    val definition = Definition(path.nameWithoutExtension, mutableListOf())
    this.filterIsInstance<JsonObject>().forEach {
        definition.definitions.add(it.parse(definition))
    }
    return definition
}
