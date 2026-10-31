package me.owdding.repo

import com.google.common.hash.Hashing
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.createDirectories
import kotlin.io.path.createParentDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText
import kotlin.io.path.writeBytes
import kotlin.io.path.writeText

private const val REMOTE_URL = "repo.owdding.me"

object RemoteRepo {

    private val gson: Gson = GsonBuilder().create()
    private lateinit var cacheDirectory: Path
    private var version: String? = null
    private var isInitialized = false
    var forceBackupRepo: Boolean = false

    fun initialize(cacheDirectory: Path, version: String = "main", callback: () -> Unit) {
        if (isInitialized) return
        RemoteRepoLogger.info("Initializing remote repo!")
        RemoteRepo.cacheDirectory = cacheDirectory
        RemoteRepo.version = version.takeUnless { it == "main" }
        val httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build()

        val currentRepoHash = cacheDirectory.resolve("index.json.sha").takeIf { it.exists() }?.readText(Charsets.UTF_8)
        val remoteRepoHash = httpClient.get("index.json.sha")

        if (remoteRepoHash == null || forceBackupRepo) {
            RemoteRepoLogger.info("Failed to load repo data, falling back to backup copy!")
            loadBackupRepo()
        } else if (currentRepoHash != remoteRepoHash) {
            RemoteRepoLogger.info("Hash missmatch, updating local copy!")
            httpClient.downloadOrUpdate(remoteRepoHash)
        }

        RemoteRepoLogger.info("Finished loading repo!")
        isInitialized = true
        callback()
    }

    fun uninitialize() {
        RemoteRepoLogger.info("Uninitializing repo!")
        isInitialized = false
    }

    fun invalidate() {
        RemoteRepoLogger.info("Invalidating repo!")
        isInitialized = false
        cacheDirectory.deleteRecursively()
    }

    fun isInitialized() = isInitialized
    fun getFileContent(file: String) = cacheDirectory.resolve(file).takeIf { it.exists() }?.readText(Charsets.UTF_8) ?: run {
        RemoteRepoLogger.warn("Requested unknown file $file from remote repo!")
        null
    }
    fun getFileContentAsJson(file: String) = getFileContent("${file.removeSuffix(".json")}.json")?.let { gson.fromJson(it, JsonElement::class.java) }

    private fun HttpClient.downloadOrUpdate(remoteHash: String) {
        cacheDirectory.createDirectories()

        val currentIndex = getFileContentAsJson("index.json")?.asJsonObject ?: JsonObject()
        RemoteRepoLogger.info("Loading remote index!")
        val remoteIndex = getJsonObject("index.json") ?: run {
            RemoteRepoLogger.error("Failed to load remote index, restoring backup copy!")
            loadBackupRepo()
            return
        }

        val cache = mutableMapOf<String, String>()
        RemoteRepoLogger.info("Loading ${remoteIndex.size()} entries")
        remoteIndex.entrySet().forEach { (key, hash) ->

            val expectedHash = hash.asString

            if (currentIndex.has(key)) {
                val storedHash = currentIndex[key].asString

                val path = cacheDirectory.resolve(key)
                val realHash = if (path.exists()) Hashing.sha256().hashBytes(path.toFile().readBytes()).toString() else null
                if (storedHash == expectedHash && realHash == expectedHash) return@forEach
            }
            cacheDirectory.resolve(key)
            RemoteRepoLogger.debug("Downloading $key!")
            cache[key] = get(key) ?: run {
                RemoteRepoLogger.error("Failed to download $key, restoring backup copy!")
                loadBackupRepo()
                return
            }
        }

        cache["index.json"] = remoteIndex.toString()
        cache["index.json.sha"] = remoteHash

        cache.forEach { (key, value) ->
            val key = cacheDirectory.resolve(key).normalize()
            if (!key.startsWith(cacheDirectory)) {
                RemoteRepoLogger.warn("Bad key location found! Skipping $key!")
                return@forEach
            }
            key.createParentDirectories()
            RemoteRepoLogger.debug("Writing $key!")
            key.writeText(value, Charsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
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

    // TODO: fix backup repo not working
    private fun loadBackupRepo() {
        cacheDirectory.deleteRecursively()
        cacheDirectory.createDirectories()
        fun error(): Nothing = error("Failed to restore backup repo!")
        val indexData = getFromBackup("index.json") ?: error()
        val index = indexData.toString(Charsets.UTF_8).toJsonObject()

        index.entrySet().forEach { (key) ->
            cacheDirectory.resolve(key).createParentDirectories().writeBytes(getFromBackup(key) ?: error(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        }
        cacheDirectory.resolve("index.json").writeBytes(indexData)
        cacheDirectory.resolve("index.json.sha").writeBytes(getFromBackup("index.json.sha") ?: error())
    }

    private fun HttpClient.getJsonObject(path: String) = get(path)?.let { gson.fromJson(it, JsonObject::class.java) }
    private fun HttpClient.get(path: String): String? = runCatching {
        if ((1..10).random() <= 2) error("")
        send(
            HttpRequest.newBuilder(URI.create("https://" + (version?.let { "$it." } ?: "") + REMOTE_URL).resolve(path))
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build(),
            HttpResponse.BodyHandlers.ofString(Charsets.UTF_8),
        ).let { it.body().takeUnless { _ -> it.statusCode() != 200 } }
    }.getOrNull()

}
