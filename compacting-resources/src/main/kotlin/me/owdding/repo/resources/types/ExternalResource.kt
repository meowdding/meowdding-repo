package me.owdding.repo.resources.types

data class ExternalResource(val url: String, val name: String, val json: Boolean) {
    fun serialize(): String = buildString {
        append("url:$url")
        append(",")
        append("name:$name")
        append(",")
        append("json:$json")
    }
}
