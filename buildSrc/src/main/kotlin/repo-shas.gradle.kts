@file:OptIn(ExperimentalPathApi::class)

import com.google.common.hash.Hashing
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlin.io.path.*

tasks.register("buildRepo") {
    val compactResources by tasks.getting
    dependsOn(compactResources)
    mustRunAfter(compactResources)
    inputs.files(compactResources.outputs.files)
    val targetDir = project.layout.buildDirectory.dir("repo")
    outputs.dir(targetDir)
    doFirst {
        val targetPath = targetDir.get().asFile.toPath()
        val compactingResourcesOutputDir = compactResources.outputs.files.first()
        val compactingResourcesOutputPath = compactingResourcesOutputDir.toPath()
        targetPath.deleteRecursively()
        targetPath.createDirectories()
        compactingResourcesOutputPath.copyToRecursively(targetPath, followLinks = false, overwrite = true)
        val map = mutableMapOf<String, String>()
        fileTree(compactingResourcesOutputDir) {
            include { true }
            forEach { file ->
                val relative = compactingResourcesOutputPath.relativize(file.toPath()).toString()
                val hash = Hashing.sha256().hashBytes(file.readBytes()).toString()
                map[relative] = hash
            }
        }

        val indexObject = JsonObject()
        map.entries.sortedBy { (key) -> key }.forEach { (key, value) ->
            indexObject.addProperty(key, value)
        }
        val index = Gson().toJson(indexObject).toByteArray(Charsets.UTF_8)
        targetPath.resolve("index.json").writeBytes(index)
        targetPath.resolve("index.json.sha").writeText(Hashing.sha256().hashBytes(index).toString())
    }
}

tasks.register("updateList") {
    val buildRepo by tasks.getting
    dependsOn(buildRepo)
    mustRunAfter(buildRepo)
    val targetFile = project.layout.projectDirectory.file("repo-list.txt")
    outputs.file(targetFile)
    doLast {
        val targetPath = buildRepo.outputs.files.first().toPath()
        val lines = buildList {
            targetPath.walk().forEach { path ->
                if (path.toFile().isFile) {
                    add(targetPath.relativize(path).toString())
                }
            }
        }.toMutableList()
        lines.sort()
        targetFile.asFile.writeText(lines.joinToString("\n"))
    }
}

