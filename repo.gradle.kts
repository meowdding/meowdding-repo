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
    compactToArray("skyocean/recipes")
    removeComments("skyocean/illegal_ingredients")
    removeComments("skyocean/ignore_duplicate_accessories")
    removeComments("skyocean/illegal_shop_recipes")

    compactToObject("pv/garden_data")
    compactToObject("pv/foraging")
    compactToObject("pv/chocolate_factory")
    compactToObject("pv/rift")
    compactToArray("pv/museum_categories")
    substituteFromDifferentFile("pv/slayer", "slayers")
    compactToObject("pv/pets/overwrites")
    compactToObject("pv/pets")
    compactToObject("pv/crimson_isle/dojo")
    compactToObject("pv/crimson_isle/kuudra")
    compactToObject("pv/crimson_isle")
    compactToArray("pv/minions/categories")
    compactToObject("pv/minions")
}
