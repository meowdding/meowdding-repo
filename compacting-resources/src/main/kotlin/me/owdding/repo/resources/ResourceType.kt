package me.owdding.repo.resources

import com.google.gson.JsonElement

abstract class ResourceType<T : JsonElement>(private val factory: () -> T, val output: String) {
    protected var value: T? = null

    abstract fun add(fileName: String, element: JsonElement)
    abstract fun getPath(): Array<String>

    open fun setup() {
        value = factory()
    }

    open fun complete(): JsonElement {
        val data = value!!
        value = null
        return data
    }

    abstract fun serialize(): String
}
