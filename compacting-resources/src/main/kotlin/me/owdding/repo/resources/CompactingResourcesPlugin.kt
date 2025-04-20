package me.owdding.repo.resources

import com.google.gson.JsonParser
import me.owdding.repo.DEFAULT_CACHE_DIRECTORY
import me.owdding.repo.FileCache
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskContainer
import org.gradle.language.jvm.tasks.ProcessResources
import java.nio.file.StandardOpenOption
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.hours

class CompactingResourcesPlugin : Plugin<Project> {
    inline fun <reified T> ExtensionContainer.create(name: String) = this.create(name, T::class.java)
    inline fun <reified T> ExtensionContainer.getByType() = this.getByType(T::class.java)
    inline fun <reified T : Task> TaskContainer.withType(config: T.() -> Unit) =
        this.withType(T::class.java).forEach { it.config() }

    inline fun Project.tree(baseDir: Any, config: ConfigurableFileTree.() -> Unit) = this.fileTree(baseDir).config()

    override fun apply(target: Project) {
        val cache = FileCache(target.gradle.gradleUserHomeDir.toPath().resolve(DEFAULT_CACHE_DIRECTORY), 1.hours)
        target.extensions.create<CompactingResourcesExtension>("compactingResources")

        target.afterEvaluate {
            target.tasks.withType<ProcessResources> {
                outputs.upToDateWhen { false }

                val configuration = target.extensions.getByType<CompactingResourcesExtension>()
                val sourceSets = target.extensions.getByType<SourceSetContainer>()
                val listOfPaths =
                    configuration.compactors.flatMap { it.getPath().toList() }.map { "${configuration.basePath}/$it" }

                val outDirectory =
                    project.layout.buildDirectory.file("generated/meowdding/compacted_resources/").get().asFile.toPath()
                outDirectory.parent.createDirectories()
                val outputBaseDirectory = outDirectory.resolve(configuration.basePath!!)

                exclude {
                    sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).resources.srcDirs.any { path ->
                        it.file.toPath().toAbsolutePath()
                            .equals(path.toPath().resolve(configuration.basePath!!).toAbsolutePath())
                    }
                }
                from(project.layout.buildDirectory.dir("generated/meowdding/compacted_resources/").get())
                sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).resources.srcDirs.add(outDirectory.toFile())

                val task = this

                doFirst {
                    val directoriesToSearch =
                        sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).resources.srcDirs.map { it.toPath() }
                            .toMutableList().apply { add(outDirectory) }

                    configuration.externalResources.forEach { resource ->
                        val orDownload = cache.getOrDownload(resource.url)

                        val contents = orDownload.toString(Charsets.UTF_8)
                        val output: String = if (resource.json) {
                            JsonParser.parseString(contents).toString()
                        } else {
                            contents
                        }
                        val path = outputBaseDirectory.resolve(resource.name)
                        path.parent.createDirectories()
                        path.writeText(
                            output,
                            options = arrayOf(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
                        )
                    }

                    configuration.compactors.forEach { compactor ->
                        compactor.setup()
                        val pathsToCompact = compactor.getPath().map { "${configuration.basePath}/$it" }
                        logger.warn("Compacting folder {}", compactor.output)

                        directoriesToSearch.forEach { file ->
                            println("Searching $file")
                            target.tree(file) {

                                include(*pathsToCompact.toTypedArray())
                                exclude(*listOfPaths.toMutableList().apply { this.removeAll(pathsToCompact) }
                                    .toTypedArray())

                                forEach {
                                    task.exclude(file.relativize(it.toPath()).toString())
                                    println("Excluding ${file.relativize(it.toPath())}")
                                    compactor.add(it.nameWithoutExtension, JsonParser.parseString(it.readText()))
                                }
                            }
                        }

                        val complete = compactor.complete()

                        val resolve = outputBaseDirectory.resolve("${compactor.output}.json")
                        resolve.parent.createDirectories()
                        resolve.writeText(
                            complete.toString(),
                            options = arrayOf(StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
                        )
                    }

                    directoriesToSearch.forEach { file ->
                        target.tree(file) {
                            include(configuration.basePath?.let { "$it/**" } ?: "")
                            exclude(*listOfPaths.toTypedArray())
                            forEach {
                                val toRelativeString =
                                    it.toRelativeString(file.resolve(configuration.basePath!!).toFile())
                                logger.warn("Compacting file {}", toRelativeString)
                                val parseString = JsonParser.parseString(it.readText())
                                val path = outputBaseDirectory.resolve(toRelativeString)
                                path.parent.createDirectories()
                                path.writeText(
                                    parseString.toString(),
                                    options = arrayOf(StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}