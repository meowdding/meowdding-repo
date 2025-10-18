package me.owdding.repo

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import kotlin.io.path.*

private const val REMOTE_URL = "repo.owdding.me"

object RemoteRepo {

    private val gson: Gson = GsonBuilder().create()
    private lateinit var cacheDirectory: Path
    private var version: String? = null
    private var isInitialized = false

    fun initialize(cacheDirectory: Path, version: String = "main", callback: () -> Unit) {
        if (isInitialized) return
        RemoteRepo.cacheDirectory = cacheDirectory
        RemoteRepo.version = version.takeUnless { it == "main" }
        val httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build()

        val currentRepoHash = cacheDirectory.resolve("index.json.sha").takeIf { it.exists() }?.readText(Charsets.UTF_8)
        val remoteRepoHash = httpClient.get("index.json.sha")

        if (remoteRepoHash == null) {
            loadBackupRepo()
        } else if (currentRepoHash != remoteRepoHash) {
            httpClient.downloadOrUpdate(remoteRepoHash)
        }

        isInitialized = true
        callback()
    }

    fun invalidate() {
        isInitialized = false
    }
    fun isInitialized() = isInitialized
    fun getFileContent(file: String) = cacheDirectory.resolve(file).takeIf { it.exists() }?.readText(Charsets.UTF_8)
    fun getFileContentAsJson(file: String) = getFileContent(file)?.let { gson.fromJson(it, JsonElement::class.java) }

    private fun HttpClient.downloadOrUpdate(remoteHash: String) {
        cacheDirectory.createDirectories()
        val currentIndex = getFileContentAsJson("index.json")?.asJsonObject ?: JsonObject()
        val remoteIndex = getJsonObject("index.json") ?: run {
            println("Failed to load repo data, falling back to backup repo!")
            loadBackupRepo()
            return
        }

        val cache = mutableMapOf<String, String>()
        cache["index.json"] = remoteIndex.toString()
        cache["index.json.sha"] = remoteHash
        remoteIndex.entrySet().forEach { (key, hash) ->
            if (currentIndex.has(key) && currentIndex[key].asString.equals(hash.asString)) return@forEach
            cache[key] = get(key) ?: run {
                println("Failed to load repo data, falling back to backup repo!")
                loadBackupRepo()
                return
            }
        }
        cache.forEach { (key, value) ->
            val key = cacheDirectory.resolve(key).normalize()
            if (!key.startsWith(cacheDirectory)) {
                println("Bad key found! Skipping $key!")
                return@forEach
            }
            key.createParentDirectories()
            key.writeText(value, Charsets.UTF_8)
        }
    }

    private fun String.toJsonObject(): JsonObject = gson.fromJson(this, JsonObject::class.java)

    private fun getFromBackup(path: String): ByteArray? = RemoteRepo::class.java.getResourceAsStream("/repo/$path")?.readAllBytes()

    private fun Path.deleteRecursively() {
        if (this.isDirectory()) {
            this.listDirectoryEntries().forEach {
                it.deleteRecursively()
            }
        }
        this.deleteIfExists()
    }

    private fun loadBackupRepo() {
        cacheDirectory.deleteRecursively()
        cacheDirectory.createDirectories()
        fun error(): Nothing = error("Failed to restore backup repo!")
        val indexData = getFromBackup("index.json") ?: error()
        val index = indexData.toString(Charsets.UTF_8).toJsonObject()

        cacheDirectory.resolve("index.json").writeBytes(indexData)
        cacheDirectory.resolve("index.json.sha").writeBytes(getFromBackup("index.json.sha") ?: error())
        index.entrySet().forEach { (key) ->
            cacheDirectory.resolve(key).writeBytes(getFromBackup(key) ?: error())
        }
    }

    private fun HttpClient.getJsonObject(path: String) = get(path)?.let { gson.fromJson(it, JsonObject::class.java) }
    private fun HttpClient.get(path: String): String? = runCatching {
        send(
            HttpRequest.newBuilder(URI.create("https://" + (version?.let { "$it." } ?: "") + REMOTE_URL).resolve(path))
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build(),
            HttpResponse.BodyHandlers.ofString(Charsets.UTF_8),
        ).let { it.body().takeUnless { _ -> it.statusCode() != 200 } }
    }.getOrNull()

}
