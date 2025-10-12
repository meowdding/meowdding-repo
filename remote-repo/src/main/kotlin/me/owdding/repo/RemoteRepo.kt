package me.owdding.repo

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import kotlin.io.path.readText

private const val REMOTE_URL = "https://repo.owdding.me/"

object RemoteRepo {

    private lateinit var cacheDirectory: Path
    private lateinit var version: String

    fun initialize(cacheDirectory: Path, version: String = "main") {
        RemoteRepo.cacheDirectory = cacheDirectory
        RemoteRepo.version = version
        val httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build()

        val currentRepoHash = cacheDirectory.resolve("index.json.sha").readText(Charsets.UTF_8)
        httpClient.get("index.json.sha")
    }

    private fun HttpClient.get(path: String) = send(
        HttpRequest.newBuilder(URI.create(REMOTE_URL).resolve(version).resolve("index.json.sha"))
            .method("GET", HttpRequest.BodyPublishers.noBody())
            .build(),
        HttpResponse.BodyHandlers.ofString(Charsets.UTF_8),
    ).let { it.body().takeUnless { _ -> it.statusCode() != 200 } }

}
