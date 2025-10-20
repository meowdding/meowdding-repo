package me.owdding.repo.resources.types

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import me.owdding.repo.resources.ResourceType

class RemoveComments(val file: String, output: String) : ResourceType<JsonElement>(::JsonArray, output) {
    override fun add(fileName: String, element: JsonElement) {
        if (fileName == file.substringAfterLast("/")) value = element
    }

    override fun getPath() = arrayOf("$file.jsonc")

    override fun serialize(): String = buildString {
        append("filename:").append(file).append(",")
        append("output:").append(output).append(",")
    }
}
