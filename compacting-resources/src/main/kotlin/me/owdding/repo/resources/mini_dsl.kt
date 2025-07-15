package me.owdding.repo.resources

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.tasks.TaskContainer
import java.io.File
import java.nio.file.Path

internal inline fun <reified T> ExtensionContainer.create(name: String, vararg args: Any) = this.create(name, T::class.java, *args)
internal inline fun <reified T> ExtensionContainer.getByType() = this.getByType(T::class.java)
internal inline fun <reified T : Task> TaskContainer.withType(config: T.() -> Unit) =
    this.withType(T::class.java).forEach { it.config() }

internal inline fun Project.tree(baseDir: Any, config: ConfigurableFileTree.() -> Unit) =
    this.fileTree(baseDir).config()

internal inline fun <reified T : Task> TaskContainer.register(name: String, vararg args: Any) =
    this.register(name, T::class.java, args)

internal inline fun <reified T : Task> TaskContainer.getByName(name: String, config: T.() -> Unit) =
    (this.getByName(name) as T).config()

internal fun RegularFile.toPath() = this.asFile.toPath()
internal fun Directory.toPath() = this.asFile.toPath()
internal fun File.relativize(other: Path) = this.toPath().relativize(other)
