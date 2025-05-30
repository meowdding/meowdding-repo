package me.owdding.repo

import org.gradle.internal.cc.base.logger
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.security.MessageDigest
import java.time.Instant
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes
import kotlin.time.Duration

const val DEFAULT_CACHE_DIRECTORY = "caches/meowdding_repo"

data class FileCache(val root: Path, val duration: Duration) {
    fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }

    fun getOrDownload(url: String): ByteArray {
        val key = getKey(url)

        if (isCached(key)) {
            return read(key)
        }
        logger.warn("Downloading {} to {}", url, key)
        val content = download(url)
        write(key, content)
        return content
    }

    fun download(url: String): ByteArray {
        val openStream = URI(url).toURL().openStream()
        return openStream.use {
            return@use openStream.readAllBytes()
        }
    }

    fun getKey(name: String): String {
        return MessageDigest.getInstance("SHA-256").digest(name.toByteArray()).toHexString()
    }

    fun isCached(key: String): Boolean {
        val resolve = root.resolve(key)

        if (resolve.exists()) {
            if (isExpired(key)) {
                Files.deleteIfExists(resolve)
                return false
            }
            return true
        }

        return false
    }

    fun isExpired(key: String): Boolean {
        val resolve = root.resolve(key)
        return Files.getLastModifiedTime(resolve).toInstant().plusMillis(duration.inWholeMilliseconds)
            .isBefore(Instant.now())
    }

    fun read(key: String): ByteArray {
        val resolve = root.resolve(key)
        if (!resolve.exists()) {
            return ByteArray(0)
        }
        return resolve.readBytes()
    }

    fun write(key: String, value: ByteArray) {
        val resolve = root.resolve(key)
        resolve.parent.createDirectories()
        resolve.writeBytes(value, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
    }

}
