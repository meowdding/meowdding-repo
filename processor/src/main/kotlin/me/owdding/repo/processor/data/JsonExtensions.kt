package me.owdding.repo.processor.data

import com.google.gson.JsonElement


private fun <T> JsonElement?.parse(default: T, mapper: (JsonElement) -> T): T = this?.runCatching {
    mapper(this)
}?.getOrNull() ?: default

internal fun JsonElement?.asNonEmptyString(): String? = parse(null) { it.asString }?.takeUnless { it.isEmpty() }
internal fun <T> JsonElement?.asList(mapper: (JsonElement) -> T): List<T> =
    parse(emptyList()) { it.asJsonArray.map(mapper) }
