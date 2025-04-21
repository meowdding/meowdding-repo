package me.owdding.repo

import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskContainer

inline fun <reified T : Task> TaskContainer.withType(crossinline configuration: T.() -> Unit = {}) =
    withType(T::class.java).configureEach { configuration(it) }

inline fun <reified T> ExtensionContainer.getByType(): T = getByType(T::class.java)
inline fun <reified T : Task> TaskContainer.register(name: String) = register(name, T::class.java)
fun SourceSetContainer.getResources(name: String = SourceSet.MAIN_SOURCE_SET_NAME) = getByName(name).resources
fun Provider<Directory>.toPath() = get().asFile.toPath()