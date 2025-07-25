package me.owdding.repo.resources.types

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import me.owdding.repo.resources.ResourceType

class SubstituteFromDifferentFile(private val folder: String, val mainFile: String, outputFile: String) :
    ResourceType<JsonElement>({ JsonArray() }, outputFile) {
    val loadedJsons = mutableMapOf<String, JsonElement>()

    override fun serialize(): String = buildString {
        append("type:substitute")
        append(",")
        append("folder:$folder")
        append(",")
        append("mainFile:$mainFile")
        append(",")
        append("output:$output")
    }

    override fun setup() {}

    override fun complete(): JsonElement {
        val mainFile = loadedJsons[mainFile]
        if (mainFile == null) {
            throw IllegalStateException("File $mainFile not found in folder $folder")
        }

        walk(mainFile)

        return mainFile
    }

    private fun walk(jsonObject: JsonElement) {
        when (jsonObject) {
            is JsonObject -> {
                for ((key, value) in jsonObject.entrySet()) {
                    if (value is JsonObject && value.has("@from")) {
                        val from = value.get("@from").asString
                        val json = loadedJsons[from]?.asJsonObject
                            ?: throw IllegalStateException("File $from not found in folder $folder")

                        val copyKey = if (value.has("key")) {
                            value.get("key").asString
                        } else {
                            "@default"
                        }

                        jsonObject.add(key, json.get(copyKey))
                    } else {
                        walk(value)
                    }
                }
            }

            is JsonArray -> {
                val map = mutableMapOf<JsonElement, JsonElement>()
                val spreadMap = mutableMapOf<JsonElement, JsonArray>()
                for (element in jsonObject) {
                    if (element is JsonObject && element.has("@from")) {
                        val from = element.get("@from").asString
                        val json = loadedJsons[from]?.asJsonObject
                            ?: throw IllegalStateException("File $from not found in folder $folder")

                        val copyKey = if (element.has("key")) {
                            element.get("key").asString
                        } else {
                            "@default"
                        }

                        val spread = element.has("spread") && element.get("spread").asBoolean

                        val toCopy = json.get(copyKey)
                        if (spread && toCopy is JsonArray) {
                            spreadMap[element] = toCopy
                            continue
                        }

                        map[element] = json.get(copyKey)
                    } else {
                        walk(element)
                    }
                }
                map.forEach { (key, value) ->
                    jsonObject.remove(key)
                    jsonObject.add(value)
                }
                spreadMap.forEach { (key, value) ->
                    jsonObject.remove(key)
                    jsonObject.addAll(value)
                }
            }
        }
    }

    override fun add(fileName: String, element: JsonElement) {
        loadedJsons[fileName] = element
    }

    override fun getPath() = arrayOf("$folder/*.json", "$folder/*.jsonc")
}
