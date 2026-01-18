package me.owdding.repo.resources

import com.google.gson.JsonParser
import me.owdding.repo.DEFAULT_CACHE_DIRECTORY
import me.owdding.repo.FileCache
import org.gradle.api.DefaultTask
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import kotlin.io.path.copyTo
import kotlin.io.path.createParentDirectories
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.hours

private const val VERSION = 0

open class CompactResourcesTask : DefaultTask() {

    init {
        val cache = FileCache(project.gradle.gradleUserHomeDir.toPath().resolve(DEFAULT_CACHE_DIRECTORY), 1.hours)
        group = "meowdding"
        val extension = project.extensions.getByType<CompactingResourcesExtension>()
        val basePath = project.layout.projectDirectory.dir("${extension.pathDirectory}/${extension.basePath}")
        val workPath = project.layout.buildDirectory.dir("tmp/meowdding/compacting-resources").get()
        val inputDirs = listOf(basePath, workPath)

        val outputPath = project.layout.buildDirectory.dir("generated/meowdding/compacting-resources").get()

        inputs.dir(basePath).withPropertyName("input")
        inputs.property("compact_resources_version", VERSION)
        inputs.property("compactors", extension.compactors.joinToString("|") { it.serialize() })
        inputs.property("external", extension.externalResources.joinToString("|") { it.serialize() })
        outputs.dir(outputPath)

        doFirst {
            extension.externalResources.forEach { externalResource ->
                val content = cache.getOrDownload(externalResource.url).decodeToString()
                val output = if (externalResource.json) JsonParser.parseString(content).toString() else content

                val path = workPath.file(externalResource.name).toPath()
                path.createParentDirectories()
                path.writeText(output, Charsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
            }

            val used: MutableList<String> = mutableListOf()

            extension.compactors.forEach { compactor ->
                compactor.setup()

                inputDirs.forEach { dir ->
                    println("Searching dir: $dir")
                    project.tree(dir) {
                        include(*compactor.getPath())

                        forEach { file ->
                            if (file.name.substringAfterLast(".") !in jsonExtensions) return@forEach

                            dir.toPath().relativize(file.toPath()).toString().also {
                                used.add(it)
                                println("Using $it")
                            }

                            compactor.add(file.nameWithoutExtension, JsonParser.parseString(file.readText()))
                        }
                    }
                }

                val output = compactor.complete()
                val outputFile = workPath.file("${compactor.output}.json").toPath()
                outputFile.createParentDirectories()
                outputFile.writeText(output.toString(), Charsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
            }

            project.tree(basePath) {
                include("**")
                exclude(used)

                forEach { file ->
                    if (file.extension !in jsonExtensions) return@forEach
                    val relative = basePath.toPath().relativize(file.toPath()).toString()

                    println("Copying file $relative")
                    val content = JsonParser.parseString(file.readText())
                    val targetPath = workPath.file(relative).toPath()
                    targetPath.createParentDirectories()
                    targetPath.writeText(content.toString(), Charsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
                }
            }

            project.tree(workPath) {
                include("**")
                exclude(used)

                forEach { file ->
                    val relative = workPath.toPath().relativize(file.toPath()).toString().also {
                        println("Including $it")
                    }
                    val output = outputPath.file(relative).toPath()
                    output.createParentDirectories()
                    file.toPath().copyTo(output, StandardCopyOption.REPLACE_EXISTING)
                }
            }
        }
    }

}
