package me.owdding.repo.resources.types

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import me.owdding.repo.resources.ResourceType

class CompactToObject(val folder: String, outputFile: String) : ResourceType<JsonObject>(::JsonObject, outputFile) {
    override fun getPath() = arrayOf("$folder/*.json", "$folder/*.jsonc")

    override fun add(fileName: String, element: JsonElement) {
        value!!.add(fileName, element)
    }

    override fun serialize(): String = buildString {
        append("type:object")
        append(",")
        append("folder:$folder")
        append(",")
        append("output:$output")
    }

}
