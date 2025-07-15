package me.owdding.repo.resources.types

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import me.owdding.repo.resources.ResourceType

class CompactToArray(private val folder: String, outputFile: String) :
    ResourceType<JsonArray>(::JsonArray, outputFile) {
    override fun getPath() = arrayOf("$folder/*.json", "$folder/*.jsonc")
    override fun serialize(): String = buildString {
        append("type:array")
        append(",")
        append("folder:$folder")
        append(",")
        append("output:$output")
    }

    override fun add(fileName: String, element: JsonElement) {
        value!!.add(element)
    }
}
