plugins {
    `repo-base`
    id("me.owdding.resources")
    id("repo-shas")
}

project.layout.buildDirectory.set(rootProject.layout.buildDirectory)

compactingResources {
    basePath = ".."

    compactToArray("foraging/hotfperks", "foraging/hotf")
    compactToArray("mining/hotmperks", "mining/hotm")
    compactToArray("accessories/families", "accessories/families")
    substituteFromDifferentFile("mining/mineshaft_corpses", "mineshaft_corpses")
}
