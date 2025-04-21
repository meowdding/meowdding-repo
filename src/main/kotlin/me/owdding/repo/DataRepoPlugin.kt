package me.owdding.repo

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.language.jvm.tasks.ProcessResources

class DataRepoPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.create("repo", RepoExtension::class.java)
        val outDirectory = target.layout.buildDirectory.dir("generated/meowdding/repo").toPath()
        val sourceSets = target.extensions.getByType<SourceSetContainer>()
        sourceSets.getResources().srcDir(outDirectory)
        val task = target.tasks.register<CreateRepoDataTask>("createRepoData")
        target.afterEvaluate {
            it.tasks.withType<ProcessResources> {
                dependsOn(task)
            }
        }
    }


}


