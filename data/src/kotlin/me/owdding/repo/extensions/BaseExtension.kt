package me.owdding.repo.extensions

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject


const val ALL = "meow_all_no_u_are_adorable_:3"

abstract class BaseExtension(val path: String) {
    val includedPaths = mutableSetOf<String>()
    val excludedPaths = mutableSetOf<String>()

    var predicate: (JsonElement) -> Boolean = {
        when (it) {
            is JsonObject -> !it.isEmpty
            is JsonArray -> !it.isEmpty
            else -> true
        }
    }

    fun withPredicate(predicate: (JsonElement) -> Boolean) = apply {
        this.predicate = predicate
    }

    fun exclude(config: MutableSet<String>.() -> Unit) {
        excludedPaths.config()
    }

    fun include(config: MutableSet<String>.() -> Unit) {
        includedPaths.config()
    }

    fun excludeAllExcept(config: MutableSet<String>.() -> Unit) {
        excludeAll()
        includedPaths.config()
    }

    fun includeAllExcept(config: MutableSet<String>.() -> Unit) {
        includeAll()
        excludedPaths.config()
    }

    fun includeAll() {
        includedPaths.add(ALL)
    }

    fun excludeAll() {
        excludedPaths.add(ALL)
    }
}