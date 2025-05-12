package me.owdding.repo.resources.types

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import me.owdding.repo.resources.ResourceType

class FileProcessor(val file: String, output: String) : ResourceType<JsonElement>(::JsonArray, output) {
    var hasInited = false

    override fun add(fileName: String, element: JsonElement) {
        if (hasInited) return
        hasInited = true
        this.value = element

        walk(element)
    }

    private fun create(variables: JsonObject, patterns: JsonArray): JsonArray {
        val meow = variables.entrySet().map { (key, value) ->
            key to if (value.isJsonArray) value.asJsonArray.asList().map { it.asString } else listOf(value.asString)
        }.map { (key, values) -> Variable(key, values) }
            .reduce { a, b -> b.apply { b.child = a } }

        val create = meow.create(listOf(mutableMapOf()))
        val actualPatterns = patterns.asSequence().map { it.asString }

        val values = JsonArray()

        create.map { it.entries.map { (key, value) -> key to value } }.forEach { variables ->
            for (element in actualPatterns) {
                var actualElement = element ?: continue
                for ((key, value) in variables) {
                    actualElement = actualElement.replace("\${$key}", value)
                }
                values.add(actualElement)
            }
        }

        return values
    }

    data class Variable(val name: String, val values: List<String>, var child: Variable? = null) {
        fun create(map: List<Map<String, String>>): List<Map<String, String>> {
            val list = mutableListOf<Map<String, String>>()

            for (item in map) {
                for (string in values) {
                    val map = item.toMutableMap()
                    map[name] = string
                    list.add(map)
                }
            }

            return child?.create(list) ?: list
        }
    }

    private fun walk(jsonObject: JsonElement) {
        when (jsonObject) {
            is JsonObject -> {
                for ((key, value) in jsonObject.entrySet()) {
                    if (value is JsonObject && value.has("@patterns")) {
                        val variables = value.getAsJsonObject("variables")
                        val patterns = value.getAsJsonArray("@patterns")

                        jsonObject.add(key, create(variables, patterns))
                    } else {
                        walk(value)
                    }
                }
            }

            is JsonArray -> {
                val map = mutableMapOf<JsonElement, JsonElement>()
                val spreadMap = mutableMapOf<JsonElement, JsonArray>()
                for (element in jsonObject) {
                    if (element is JsonObject && element.has("@patterns")) {
                        val variables = element.getAsJsonObject("variables")
                        val patterns = element.getAsJsonArray("@patterns")

                        create(variables, patterns)

                        val spread = element.has("spread") && element.get("spread").asBoolean

                        if (spread) {
                            spreadMap[element] = create(variables, patterns)
                            continue
                        }

                        map[element] = create(variables, patterns)
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

    override fun getPath() = arrayOf("${file}.json", "${file}.jsonc")
}