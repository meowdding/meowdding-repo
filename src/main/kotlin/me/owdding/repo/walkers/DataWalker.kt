package me.owdding.repo.walkers

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import me.owdding.repo.ConfigContext


interface DataWalker {

    companion object {
        fun copy(origin: JsonElement, configContext: ConfigContext): JsonElement {
            return when (origin) {
                is JsonObject -> JsonObjectWalker(origin, configContext)
                is JsonArray -> JsonArrayWalker(origin, configContext)
                else -> throw UnsupportedOperationException("Got json null or primitive, which both aren't supported")
            }.process()
        }
    }

    fun process(): JsonElement

}