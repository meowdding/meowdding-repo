plugins {
    `kotlin-logic`
}

evaluationDependsOn(":repo")

tasks.processResources {
    with(copySpec {
        from(tasks.getByPath(":repo:buildRepo").outputs)
        into("repo")
    })
}
