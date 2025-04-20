package me.owdding.repo

import com.google.gson.JsonParser
import me.owdding.repo.extensions.ALL
import me.owdding.repo.extensions.BaseExtension
import me.owdding.repo.walkers.DataWalker
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.language.jvm.tasks.ProcessResources
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

class DataRepoPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.create("repo", RepoExtension::class.java)
        target.afterEvaluate {
            val outDirectory = target.layout.buildDirectory.dir("generated/meowdding/repo").toPath()
            outDirectory.createDirectories()
            val sourceSets = target.extensions.getByType<SourceSetContainer>()
            sourceSets.getResources().srcDir(outDirectory)
            val extension = target.extensions.getByType<RepoExtension>()
            val collectInitialized = extension.collectInitialized()
            it.tasks.withType<ProcessResources> {
                doFirst {
                    collectInitialized.forEach { createData(outDirectory, it, extension) }
                }
            }
        }
    }

    fun createData(outDirectory: Path, config: BaseExtension, extension: RepoExtension) {
        if (config.includedPaths.contains(ALL) && config.excludedPaths.contains(ALL)) {
            config.includedPaths.remove(ALL)
            config.excludedPaths.remove(ALL)
        }
        if (config.excludedPaths.isEmpty() && config.includedPaths.isEmpty()) {
            return
        }
        if (config.excludedPaths.contains(ALL) && config.includedPaths.isEmpty()) {
            return
        }
        val configContext = ConfigContext(config.includedPaths, config.excludedPaths, ALL, config.predicate)
        val data = BaseExtension::class.java.classLoader.getResourceAsStream("data/${config.path}.json")?.readAllBytes()
            ?.toString(Charsets.UTF_8)
        data ?: throw NullPointerException("data/${config.path}.json is null")
        val copy = DataWalker.copy(JsonParser.parseString(data), configContext)
        val resolve = outDirectory.resolve(extension.dataPath).resolve("${config.path}.json")
        resolve.parent.createDirectories()
        resolve.writeText(
            text = copy.toString(),
            charset = Charsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
    }
}


